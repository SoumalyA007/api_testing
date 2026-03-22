package testData;

import payloads.request.LoginRequestPOJO;

import java.util.HashMap;
import java.util.Map;

public class AuthTestDataFactory {

    public static LoginRequestPOJO validLogin(String username, String password) {
        return LoginRequestPOJO.builder()
                .username(username)
                .password(password)
                .build();
    }

    public static LoginRequestPOJO loginWithoutPassword(String username) {
        return LoginRequestPOJO.builder()
                .username(username)
                .build();
    }

    public static LoginRequestPOJO loginWithoutUsername(String password) {
        return LoginRequestPOJO.builder()
                .password(password)
                .build();
    }

    public static Map<String, Object> jsonInjectionPayload() {

        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> injection = new HashMap<>();
        injection.put("$ne", null);

        payload.put("username", injection);
        payload.put("password", injection);

        return payload;
    }

    public static LoginRequestPOJO sqlInjectionPayload() {
        return validLogin("' OR 1=1 --", "' OR 1=1 --");
    }

    public static LoginRequestPOJO xssPayload() {
        return validLogin("<script>alert(1)</script>", "password123");
    }
}