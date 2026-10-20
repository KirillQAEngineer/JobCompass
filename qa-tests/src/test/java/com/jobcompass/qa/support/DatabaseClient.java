package com.jobcompass.qa.support;

import com.jobcompass.qa.config.TestConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public final class DatabaseClient {
    private DatabaseClient() {
    }

    public static void verifyEmail(String email) {
        updateUser(
                "UPDATE users SET email_verified_at = CURRENT_TIMESTAMP, "
                        + "email_verification_required = false, "
                        + "email_verification_token_hash = NULL, "
                        + "email_verification_expires_at = NULL WHERE email = ?",
                email
        );
    }

    public static UUID createVerifiedUser(TestUser user) {
        UUID publicId = UUID.randomUUID();
        String sql = "INSERT INTO users "
                + "(public_id, email, hashed_password, full_name, is_admin, "
                + "email_verification_required, email_verified_at, "
                + "analytics_lifetime_access, created_at) "
                + "VALUES (?, ?, ?, ?, false, false, CURRENT_TIMESTAMP, false, CURRENT_TIMESTAMP)";

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, publicId);
            statement.setString(2, user.email());
            statement.setString(3, hashPassword(user.password()));
            statement.setString(4, user.fullName());
            statement.executeUpdate();
            return publicId;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not create the test user", exception);
        }
    }

    public static void grantAdmin(String email) {
        updateUser("UPDATE users SET is_admin = true WHERE email = ?", email);
    }

    public static void grantAnalytics(String email) {
        updateUser(
                "UPDATE users SET analytics_lifetime_access = true WHERE email = ?",
                email
        );
    }

    private static void updateUser(String sql, String email) {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            int rows = statement.executeUpdate();
            if (rows != 1) {
                throw new IllegalStateException("Expected one test user, updated " + rows);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not prepare the test user", exception);
        }
    }

    private static Connection connection() throws SQLException {
        return DriverManager.getConnection(
                TestConfig.jdbcUrl(),
                TestConfig.databaseUser(),
                TestConfig.databasePassword()
        );
    }

    private static String hashPassword(String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        char[] value = password.toCharArray();
        try {
            return argon2.hash(2, 65_536, 1, value);
        } finally {
            argon2.wipeArray(value);
        }
    }
}
