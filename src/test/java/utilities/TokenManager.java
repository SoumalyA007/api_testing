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

    private static String token;

    private static Map<UserRole, String> tokenStore = new HashMap<>();

    private static final String SECRET = "my_super_secret_key_which_is_long_enough_12345";

    public static String getToken(UserRole role) {

        if (!tokenStore.containsKey(role)) {
            generateToken(role);
        }

        return tokenStore.get(role);
    }

    public static void generateToken(UserRole role){

        String email="";
        String password="";

        switch (role){
            case ADMIN:
                email = "admin@enterprise.com";
                password = "password123";
                break;
            case USER:
                email = "tester@qa.com";
                password ="password123";
                break;
            default:
                System.out.println("The role you entered is not available");
        }

        LoginRequestPOJO request = LoginRequestPOJO.builder()
                .email(email)
                .password(password)
                .build();

        Response response = Auth.login(request);

        LoginResponsePOJO loginResponsePOJO = response.as(LoginResponsePOJO.class);

        token = loginResponsePOJO.getToken();
        tokenStore.put(role, token);
    }


    public static String generateExpiredToken(UserRole role) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

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