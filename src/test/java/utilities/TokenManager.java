package utilities;

import enums.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.restassured.response.Response;
import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;
import endpoints.Auth;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenManager {

    // 🔥 Thread-safe storage
    private static ThreadLocal<Map<UserRole, String>> tokenStore =
            ThreadLocal.withInitial(HashMap::new);

    private static ThreadLocal<Map<UserRole, Integer>> userIdStore =
            ThreadLocal.withInitial(HashMap::new);

    private static final String SECRET =
            "my_super_secret_key_which_is_long_enough_12345";

    // ================= GET TOKEN =================
    public static String getToken(UserRole role) {

        Map<UserRole, String> tokens = tokenStore.get();

        if (!tokens.containsKey(role)) {
            generateToken(role);
        }

        return tokens.get(role);
    }

    // ================= GET USER ID =================
    public static int getUserId(UserRole role) {

        Map<UserRole, Integer> users = userIdStore.get();

        if (!users.containsKey(role)) {
            generateToken(role);
        }

        return users.get(role);
    }

    // ================= GENERATE TOKEN =================
    private static void generateToken(UserRole role){

        String username="";
        String password="";

        switch (role){
            case ADMIN:
                username = "admin1";
                password = "password123";
                break;
            case USER:
                username = "testuser";
                password ="password123";
                break;
            default:
                throw new RuntimeException("Invalid role");
        }

        LoginRequestPOJO request = LoginRequestPOJO.builder()
                .username(username)
                .password(password)
                .build();

        Response response = Auth.login(request);

        LoginResponsePOJO loginResponsePOJO =
                response.as(LoginResponsePOJO.class);

        String token = loginResponsePOJO.getToken();
        int userId = loginResponsePOJO.getUserId();

        // 🔥 Store in THREAD LOCAL
        tokenStore.get().put(role, token);
        userIdStore.get().put(role, userId);
    }

    // ================= CLEAR AFTER TEST =================
    public static void clear() {
        tokenStore.remove();
        userIdStore.remove();
    }

    // ================= EXPIRED TOKEN =================
    public static String generateExpiredToken(UserRole role) {

        Key key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8));

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claim("id", role == UserRole.ADMIN ? 1 : 2)
                .claim("role", role.name().toLowerCase())
                .setIssuedAt(new Date(now - 10000))
                .setExpiration(new Date(now - 5000))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }
}