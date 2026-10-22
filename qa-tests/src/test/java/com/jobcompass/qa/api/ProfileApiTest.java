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
@Feature("Candidate profile")
class ProfileApiTest {

    @Test
    void userCanCreateProfileWithoutResumeAndKeepManualFields() {
        TestUser user = ApiSteps.createVerifiedUser("profile");

        Response missing = ApiSteps.authenticated(user).get("/profile/me");
        assertThat(missing.statusCode()).isEqualTo(404);

        Response created = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(ApiSteps.profilePayload())
                .put("/profile/me");
        assertThat(created.statusCode()).isEqualTo(200);
        assertThat(created.jsonPath().getString("profession")).isEqualTo("QA Engineer");
        assertThat(created.jsonPath().getString("skills"))
                .isEqualTo("API Testing, SQL");

        Response clearedResume = ApiSteps.authenticated(user).delete("/profile/me/resume");
        assertThat(clearedResume.statusCode()).isEqualTo(200);
        assertThat(clearedResume.jsonPath().getString("profession")).isEqualTo("QA Engineer");
        assertThat(clearedResume.jsonPath().getString("technologies"))
                .isEqualTo("Postman, Selenium");
    }

    @Test
    void profileInputIsNormalizedAndValidated() {
        TestUser user = ApiSteps.createVerifiedUser("profile-validation");
        Map<String, Object> payload = Map.of(
                "profession", "  QA Engineer  ",
                "level", " Middle ",
                "skills", List.of(" SQL ", "sql", "", "API Testing"),
                "technologies", List.of(" Selenium ", "selenium", "Postman"),
                "english_level", " B1 ",
                "preferred_roles", List.of(" QA Engineer ", "qa engineer")
        );

        Response response = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(payload)
                .put("/profile/me");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("profession")).isEqualTo("QA Engineer");
        assertThat(response.jsonPath().getString("skills"))
                .isEqualTo("SQL, API Testing");

        Response invalid = ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "profession", " ",
                        "level", "Middle",
                        "skills", List.of(),
                        "technologies", List.of(),
                        "english_level", "B1",
                        "preferred_roles", List.of()
                ))
                .put("/profile/me");
        assertThat(invalid.statusCode()).isEqualTo(422);
    }

    @Test
    void uploadRequiresAFileAndAuthentication() {
        assertThat(ApiSteps.request().post("/upload/").statusCode()).isEqualTo(401);

        TestUser user = ApiSteps.createVerifiedUser("upload");
        assertThat(ApiSteps.authenticated(user).post("/upload/").statusCode())
                .isEqualTo(422);
    }
}
