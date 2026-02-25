package utilities;

import enums.UserRole;
import io.restassured.response.Response;
import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;
import endpoints.Auth;

import java.util.HashMap;
import java.util.Map;

public class TokenManager {

    private static String token;

    private static Map<UserRole, String> tokenStore = new HashMap<>();

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

}