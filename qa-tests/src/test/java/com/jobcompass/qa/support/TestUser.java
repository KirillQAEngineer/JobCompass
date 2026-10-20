package com.jobcompass.qa.support;

public record TestUser(String email, String password, String fullName, String token) {
    public TestUser withToken(String accessToken) {
        return new TestUser(email, password, fullName, accessToken);
    }
}
