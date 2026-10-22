package com.jobcompass.qa.config;

public final class TestConfig {
    private TestConfig() {
    }

    public static String apiBaseUrl() {
        return value("api.base.url", "API_BASE_URL", "http://127.0.0.1:8000");
    }

    public static String webBaseUrl() {
        return value("web.base.url", "WEB_BASE_URL", "http://127.0.0.1:3000");
    }

    public static String jdbcUrl() {
        return value(
                "jdbc.database.url",
                "JDBC_DATABASE_URL",
                "jdbc:postgresql://127.0.0.1:5432/jobcompass_ci"
        );
    }

    public static String databaseUser() {
        return value("database.user", "DATABASE_USER", "jobcompass");
    }

    public static String databasePassword() {
        return value("database.password", "DATABASE_PASSWORD", "jobcompass");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(value("headless", "HEADLESS", "true"));
    }

    public static String secretKey() {
        return value(
                "secret.key",
                "SECRET_KEY",
                "ci-only-secret-key-with-at-least-32-characters"
        );
    }

    private static String value(String property, String environment, String fallback) {
        String systemValue = System.getProperty(property);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String environmentValue = System.getenv(environment);
        return environmentValue == null || environmentValue.isBlank()
                ? fallback
                : environmentValue;
    }
}
