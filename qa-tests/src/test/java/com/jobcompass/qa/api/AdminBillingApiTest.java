package com.jobcompass.qa.api;

import com.jobcompass.qa.support.ApiSteps;
import com.jobcompass.qa.support.TestUser;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
@Epic("JobCompass API")
class AdminBillingApiTest {

    @Test
    @Feature("Administration")
    void administratorCanInspectUsersAndManageRolesAndAccess() {
        TestUser admin = ApiSteps.createAdmin("admin");
        TestUser user = ApiSteps.createVerifiedUser("managed-user");

        Response forbidden = ApiSteps.authenticated(user).get("/admin/users");
        assertThat(forbidden.statusCode()).isEqualTo(403);

        Map<String, Object> stats = ApiSteps.authenticated(admin)
                .get("/admin/stats")
                .then().statusCode(200)
                .extract().as(Map.class);
        assertThat((Integer) stats.get("total_users")).isGreaterThanOrEqualTo(2);
        assertThat((Integer) stats.get("total_admins")).isGreaterThanOrEqualTo(1);

        List<Map<String, Object>> users = ApiSteps.authenticated(admin)
                .get("/admin/users")
                .then().statusCode(200)
                .extract().jsonPath().getList("$");
        Map<String, Object> managed = users.stream()
                .filter(item -> user.email().equals(item.get("email")))
                .findFirst()
                .orElseThrow();
        String userId = String.valueOf(managed.get("id"));

        Map<String, Object> detail = ApiSteps.authenticated(admin)
                .get("/admin/users/{id}", userId)
                .then().statusCode(200)
                .extract().as(Map.class);
        assertThat(detail.get("email")).isEqualTo(user.email());
        assertThat(detail.get("analytics_lifetime_access")).isEqualTo(false);

        Response access = ApiSteps.authenticated(admin)
                .contentType(ContentType.JSON)
                .body(Map.of("analytics_lifetime_access", true))
                .patch("/admin/users/{id}/analytics-access", userId);
        assertThat(access.statusCode()).isEqualTo(200);
        assertThat(access.jsonPath().getBoolean("analytics_lifetime_access")).isTrue();

        Response role = ApiSteps.authenticated(admin)
                .contentType(ContentType.JSON)
                .body(Map.of("is_admin", true))
                .patch("/admin/users/{id}/role", userId);
        assertThat(role.statusCode()).isEqualTo(200);
        assertThat(role.jsonPath().getBoolean("is_admin")).isTrue();
        assertThat(ApiSteps.authenticated(user).get("/admin/stats").statusCode())
                .isEqualTo(200);

        String adminId = ApiSteps.authenticated(admin)
                .get("/auth/me")
                .jsonPath().getString("id");
        Response selfRemoval = ApiSteps.authenticated(admin)
                .contentType(ContentType.JSON)
                .body(Map.of("is_admin", false))
                .patch("/admin/users/{id}/role", adminId);
        assertThat(selfRemoval.statusCode()).isEqualTo(400);
    }

    @Test
    @Feature("Billing")
    void billingContractUsesUsdtMinimumAndRejectsInvalidWebhook() {
        TestUser user = ApiSteps.createVerifiedUser("billing");
        Response status = ApiSteps.authenticated(user).get("/billing/me");
        assertThat(status.statusCode()).isEqualTo(200);
        assertThat(status.jsonPath().getBoolean("has_analytics_access")).isFalse();
        assertThat(status.jsonPath().getString("display_price")).isEqualTo("from 1 USDT");
        assertThat(status.jsonPath().getInt("price_minor_units")).isEqualTo(100);

        Response belowMinimum = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(Map.of("amount_usdt", "0.99"))
                .post("/billing/analytics-lifetime/checkout");
        assertThat(belowMinimum.statusCode()).isEqualTo(422);

        Response unavailableProvider = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(Map.of("amount_usdt", "1.00"))
                .post("/billing/analytics-lifetime/checkout");
        assertThat(unavailableProvider.statusCode()).isEqualTo(503);

        Response invalidWebhook = ApiSteps.request()
                .contentType(ContentType.JSON)
                .header("x-nowpayments-sig", "invalid")
                .body(Map.of("invoice_id", "invoice", "payment_id", "payment"))
                .post("/billing/nowpayments/ipn");
        assertThat(invalidWebhook.statusCode()).isEqualTo(401);
    }
}
