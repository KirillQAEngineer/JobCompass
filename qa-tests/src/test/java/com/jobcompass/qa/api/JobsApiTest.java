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
@Feature("Vacancy interactions")
class JobsApiTest {

    @Test
    void savedAndAppliedVacanciesPreserveMetadataAndRemainUserScoped() {
        TestUser user = ApiSteps.createVerifiedUser("jobs");
        String externalId = UUID.randomUUID().toString();
        Map<String, Object> vacancy = vacancy(externalId, "like");

        Response saved = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(vacancy)
                .post("/jobs/interact");
        assertThat(saved.statusCode()).isEqualTo(200);
        assertThat(saved.jsonPath().getString("job_location")).isEqualTo("Remote");

        List<Map<String, Object>> savedJobs = ApiSteps.authenticated(user)
                .get("/jobs/saved")
                .then().statusCode(200)
                .extract().jsonPath().getList("$");
        assertThat(savedJobs).hasSize(1);
        assertThat(savedJobs.getFirst()).doesNotContainKey("user_id");
        assertThat(savedJobs.getFirst().get("job_external_id")).isEqualTo(externalId);

        ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(vacancy(externalId + "-applied", "apply"))
                .post("/jobs/interact")
                .then().statusCode(200);
        assertThat(ApiSteps.authenticated(user)
                .get("/jobs/applied")
                .jsonPath().getList("$")).hasSize(1);

        TestUser other = ApiSteps.createVerifiedUser("jobs-other");
        assertThat(ApiSteps.authenticated(other)
                .get("/jobs/saved")
                .jsonPath().getList("$")).isEmpty();

        Response deleted = ApiSteps.authenticated(user)
                .queryParam("job_url", vacancy.get("job_url"))
                .delete("/jobs/saved");
        assertThat(deleted.statusCode()).isEqualTo(200);
        assertThat(ApiSteps.authenticated(user)
                .queryParam("job_url", vacancy.get("job_url"))
                .delete("/jobs/saved").statusCode()).isEqualTo(404);
    }

    @Test
    void commentsAreNormalizedUpdatedAndDeletedByStableIdentity() {
        TestUser user = ApiSteps.createVerifiedUser("comments");
        String externalId = UUID.randomUUID().toString();

        Map<String, Object> first = Map.of(
                "job_source", "  REMOTIVE  ",
                "job_external_id", "  " + externalId + "  ",
                "comment", "  Initial comment  "
        );
        Response created = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(first)
                .put("/jobs/comments");
        assertThat(created.statusCode()).isEqualTo(200);
        assertThat(created.jsonPath().getString("job_source")).isEqualTo("remotive");
        assertThat(created.jsonPath().getString("comment")).isEqualTo("Initial comment");

        ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "job_source", "remotive",
                        "job_external_id", externalId,
                        "comment", "Updated comment"
                ))
                .put("/jobs/comments")
                .then().statusCode(200);

        List<Map<String, Object>> comments = ApiSteps.authenticated(user)
                .get("/jobs/comments")
                .jsonPath().getList("$");
        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().get("comment")).isEqualTo("Updated comment");

        Response removed = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "job_source", "remotive",
                        "job_external_id", externalId,
                        "comment", ""
                ))
                .put("/jobs/comments");
        assertThat(removed.statusCode()).isEqualTo(200);
        assertThat(ApiSteps.authenticated(user)
                .get("/jobs/comments")
                .jsonPath().getList("$")).isEmpty();
    }

    @Test
    void invalidCommentContractIsRejected() {
        TestUser user = ApiSteps.createVerifiedUser("comment-validation");
        Response blankIdentity = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "job_source", " ",
                        "job_external_id", "id",
                        "comment", "text"
                ))
                .put("/jobs/comments");
        assertThat(blankIdentity.statusCode()).isEqualTo(422);

        Response tooLong = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "job_source", "source",
                        "job_external_id", "id",
                        "comment", "x".repeat(2001)
                ))
                .put("/jobs/comments");
        assertThat(tooLong.statusCode()).isEqualTo(422);
    }

    private static Map<String, Object> vacancy(String externalId, String action) {
        return Map.of(
                "job_title", "Senior QA Engineer",
                "job_company", "JobCompass Test Company",
                "job_url", "https://example.com/jobs/" + externalId,
                "job_location", "Remote",
                "job_work_format", "Remote",
                "job_published_at", "2026-10-16T10:00:00Z",
                "job_description", "API and UI testing with Java and Selenium",
                "job_source", "Remotive",
                "job_external_id", externalId,
                "action", action
        );
    }
}
