package com.jobcompass.qa.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcompass.qa.config.TestConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

final class TokenFactory {
    private static final ObjectMapper JSON = new ObjectMapper();

    private TokenFactory() {
    }

    static String create(UUID userId) {
        long issuedAt = Instant.now().getEpochSecond();
        String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
        String payload = encodeJson(Map.of(
                "sub", userId.toString(),
                "iss", "jobcompass-api",
                "aud", "jobcompass-web",
                "iat", issuedAt,
                "exp", issuedAt + 3_600,
                "jti", UUID.randomUUID().toString(),
                "type", "access"
        ));
        String unsigned = header + "." + payload;
        return unsigned + "." + sign(unsigned);
    }

    private static String encodeJson(Map<String, Object> value) {
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(JSON.writeValueAsBytes(value));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not create the test JWT", exception);
        }
    }

    private static String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    TestConfig.secretKey().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign the test JWT", exception);
        }
    }
}
