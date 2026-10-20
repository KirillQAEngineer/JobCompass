package com.jobcompass.qa.support;

import com.jobcompass.qa.config.TestConfig;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.UUID;

public final class ApiSteps {
    private ApiSteps() {
    }

    public static TestUser newUser(String prefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return new TestUser(
                prefix + "." + suffix + "@example.com",
                "Password-123",
                "Java QA " + suffix,
                null
        );
    }

    public static Response register(TestUser user) {
        return Allure.step("Register a new account", () -> request()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", user.email(),
                        "password", user.password(),
                        "full_name", user.fullName()
                ))
                .post("/auth/register"));
    }

    public static TestUser createVerifiedUser(String prefix) {
        TestUser user = newUser(prefix);
        UUID publicId = DatabaseClient.createVerifiedUser(user);
        return user.withToken(TokenFactory.create(publicId));
    }

    public static TestUser createAdmin(String prefix) {
        TestUser user = newUser(prefix);
        UUID publicId = DatabaseClient.createVerifiedUser(user);
        DatabaseClient.grantAdmin(user.email());
        return user.withToken(TokenFactory.create(publicId));
    }

    public static TestUser createAnalyticsUser(String prefix) {
        TestUser user = newUser(prefix);
        UUID publicId = DatabaseClient.createVerifiedUser(user);
        DatabaseClient.grantAnalytics(user.email());
        return user.withToken(TokenFactory.create(publicId));
    }

    public static TestUser login(TestUser user) {
        Response response = Allure.step("Sign in through the API", () -> request()
                .contentType(ContentType.URLENC)
                .formParam("username", user.email())
                .formParam("password", user.password())
                .post("/auth/login"));
        response.then().statusCode(200);
        return user.withToken(response.jsonPath().getString("access_token"));
    }

    public static RequestSpecification request() {
        return RestAssured.given()
                .baseUri(TestConfig.apiBaseUrl())
                .accept(ContentType.JSON);
    }

    public static RequestSpecification authenticated(TestUser user) {
        return request().auth().oauth2(user.token());
    }

    public static Map<String, Object> profilePayload() {
        return Map.of(
                "profession", "QA Engineer",
                "level", "Middle",
                "skills", java.util.List.of("API Testing", "SQL"),
                "technologies", java.util.List.of("Postman", "Selenium"),
                "english_level", "B1",
                "preferred_roles", java.util.List.of("QA Engineer", "Test Engineer")
        );
    }
}
