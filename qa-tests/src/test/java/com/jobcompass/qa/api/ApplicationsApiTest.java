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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
@Epic("JobCompass API")
@Feature("Analytics and applications")
class ApplicationsApiTest {

    @Test
    void applicationLifecycleUpdatesStatisticsAndArchive() {
        TestUser user = ApiSteps.createAnalyticsUser("applications");
        Map<String, Object> vacancy = applicationPayload();

        Response first = createApplication(user, vacancy);
        Response duplicate = createApplication(user, vacancy);
        assertThat(first.statusCode()).isEqualTo(200);
        assertThat(duplicate.statusCode()).isEqualTo(200);
        assertThat(duplicate.jsonPath().getInt("id")).isEqualTo(first.jsonPath().getInt("id"));

        int applicationId = first.jsonPath().getInt("id");
        List<Map<String, Object>> active = ApiSteps.authenticated(user)
                .get("/applications")
                .then().statusCode(200)
                .extract().jsonPath().getList("$");
        assertThat(active).hasSize(1);

        ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(Map.of("status", "technical_interview"))
                .patch("/applications/{id}/status", applicationId)
                .then().statusCode(200);

        Map<String, Object> stats = ApiSteps.authenticated(user)
                .get("/applications/stats")
                .then().statusCode(200)
                .extract().as(Map.class);
        assertThat(stats.get("total_applications")).isEqualTo(1);
        assertThat(stats.get("technical_interview_in_progress")).isEqualTo(1);
        assertThat(stats.get("active_processes")).isEqualTo(1);

        Response adjusted = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "total_applications", 42,
                        "total_interviews", 7,
                        "total_offers", 2
                ))
                .patch("/applications/stats/analytics");
        assertThat(adjusted.statusCode()).isEqualTo(200);
        assertThat(adjusted.jsonPath().getInt("total_applications")).isEqualTo(42);
        assertThat(adjusted.jsonPath().getInt("technical_interview_in_progress"))
                .isEqualTo(1);

        ApiSteps.authenticated(user)
                .patch("/applications/{id}/archive", applicationId)
                .then().statusCode(200);
        assertThat(ApiSteps.authenticated(user)
                .get("/applications")
                .jsonPath().getList("$")).isEmpty();
        assertThat(ApiSteps.authenticated(user)
                .get("/applications/archived")
                .jsonPath().getList("$")).hasSize(1);

        ApiSteps.authenticated(user)
                .patch("/applications/{id}/unarchive", applicationId)
                .then().statusCode(200);
        assertThat(ApiSteps.authenticated(user)
                .get("/applications")
                .jsonPath().getList("$")).hasSize(1);
    }

    @Test
    void applicationsAreProtectedBySubscriptionOwnershipAndValidation() {
        TestUser owner = ApiSteps.createAnalyticsUser("application-owner");
        TestUser other = ApiSteps.createAnalyticsUser("application-other");
        int applicationId = createApplication(owner, applicationPayload())
                .jsonPath().getInt("id");

        Response foreignUpdate = ApiSteps.authenticated(other)
                .contentType(ContentType.JSON)
                .body(Map.of("status", "offer"))
                .patch("/applications/{id}/status", applicationId);
        assertThat(foreignUpdate.statusCode()).isEqualTo(404);

        Response invalidStatus = ApiSteps.authenticated(owner)
                .contentType(ContentType.JSON)
                .body(Map.of("status", "unknown"))
                .patch("/applications/{id}/status", applicationId);
        assertThat(invalidStatus.statusCode()).isEqualTo(422);

        Response invalidTotal = ApiSteps.authenticated(owner)
                .contentType(ContentType.JSON)
                .body(Map.of("total_applications", -1))
                .patch("/applications/stats/analytics");
        assertThat(invalidTotal.statusCode()).isEqualTo(422);

        TestUser locked = ApiSteps.createVerifiedUser("application-locked");
        assertThat(ApiSteps.authenticated(locked).get("/applications").statusCode())
                .isEqualTo(403);
    }

    private static Response createApplication(TestUser user, Map<String, Object> payload) {
        return ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/applications");
    }

    private static Map<String, Object> applicationPayload() {
        String id = UUID.randomUUID().toString();
        return Map.of(
                "job_title", "QA Automation Engineer",
                "job_company", "JobCompass Test Company",
                "job_url", "https://example.com/application/" + id,
                "job_location", "Remote",
                "job_work_format", "Remote",
                "job_published_at", "2026-10-16T10:00:00Z",
                "job_description", "Java, Selenium, REST Assured",
                "job_source", "java-tests",
                "job_external_id", id
        );
    }
}
