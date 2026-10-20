package com.jobcompass.qa.api;

import com.jobcompass.qa.support.ApiSteps;
import com.jobcompass.qa.support.DatabaseClient;
import com.jobcompass.qa.support.TestUser;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
@Epic("JobCompass API")
@Feature("Authentication and security")
class AuthSecurityApiTest {

    @Test
    void registrationRequiresEmailVerificationAndRejectsDuplicates() {
        TestUser user = ApiSteps.newUser("registration");

        Response registration = ApiSteps.register(user);
        assertThat(registration.statusCode()).isEqualTo(202);
        assertThat(registration.jsonPath().getString("email")).isEqualTo(user.email());

        Response unverifiedLogin = ApiSteps.request()
                .contentType(ContentType.URLENC)
                .formParam("username", user.email())
                .formParam("password", user.password())
                .post("/auth/login");
        assertThat(unverifiedLogin.statusCode()).isEqualTo(403);
        assertThat(unverifiedLogin.jsonPath().getString("detail"))
                .isEqualTo("Email verification required");

        DatabaseClient.verifyEmail(user.email());
        TestUser authenticated = ApiSteps.login(user);
        Map<String, Object> account = ApiSteps.authenticated(authenticated)
                .get("/auth/me")
                .then()
                .statusCode(200)
                .extract()
                .as(Map.class);

        assertThat(account.get("email")).isEqualTo(user.email());
        assertThatCodeIsUuid(account.get("id"));

        assertThat(ApiSteps.register(user).statusCode()).isEqualTo(409);
    }

    @Test
    void registrationEnforcesPasswordBoundary() {
        TestUser weak = ApiSteps.newUser("weak");
        Response weakResponse = ApiSteps.request()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", weak.email(),
                        "password", "12345678",
                        "full_name", weak.fullName()
                ))
                .post("/auth/register");
        assertThat(weakResponse.statusCode()).isEqualTo(422);

        TestUser accepted = ApiSteps.newUser("nine-chars");
        Response acceptedResponse = ApiSteps.request()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", accepted.email(),
                        "password", "123456789",
                        "full_name", accepted.fullName()
                ))
                .post("/auth/register");
        assertThat(acceptedResponse.statusCode()).isEqualTo(202);
    }

    @Test
    void protectedEndpointsAndSecurityHeadersAreEnforced() {
        Response health = ApiSteps.request().get("/");
        assertThat(health.statusCode()).isEqualTo(200);
        assertThat(health.jsonPath().getString("status")).isEqualTo("ok");
        assertThat(health.header("Cache-Control")).isEqualTo("no-store");
        assertThat(health.header("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(health.header("X-Frame-Options")).isEqualTo("DENY");
        assertThat(health.header("Referrer-Policy")).isEqualTo("no-referrer");

        assertThat(ApiSteps.request().get("/auth/me").statusCode()).isEqualTo(401);
        assertThat(ApiSteps.request()
                .auth().oauth2("not-a-jwt")
                .get("/profile/me")
                .statusCode()).isEqualTo(401);

        Response hostileOrigin = ApiSteps.request()
                .header("Origin", "https://attacker.invalid")
                .header("Access-Control-Request-Method", "GET")
                .options("/profile/me");
        assertThat(hostileOrigin.header("Access-Control-Allow-Origin")).isNull();
    }

    private static void assertThatCodeIsUuid(Object value) {
        UUID parsed = UUID.fromString(String.valueOf(value));
        assertThat(parsed.version()).isBetween(1, 5);
    }
}
