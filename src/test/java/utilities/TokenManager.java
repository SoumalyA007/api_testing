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

    private static Map<UserRole, String> tokenStore = new HashMap<>();
    private static Map<UserRole, Integer> userIdStore = new HashMap<>();

    private static final String SECRET =
            "my_super_secret_key_which_is_long_enough_12345";

    public static String getToken(UserRole role) {

        if (!tokenStore.containsKey(role)) {
            generateToken(role);
        }

        return tokenStore.get(role);
    }

    public static int getUserId(UserRole role) {

        if (!userIdStore.containsKey(role)) {
            generateToken(role);
        }

        return userIdStore.get(role);
    }

    public static void generateToken(UserRole role){

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

        tokenStore.put(role, token);
        userIdStore.put(role, userId);
    }

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