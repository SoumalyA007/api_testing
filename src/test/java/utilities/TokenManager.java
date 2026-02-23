package utilities;

import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;
import endpoints.Auth;

public class TokenManager {

    private static String token;

    public static void generateToken(String email, String password){

        LoginRequestPOJO request = LoginRequestPOJO.builder()
                .email(email)
                .password(password)
                .build();

        LoginResponsePOJO response = Auth.login(request);

        token = response.getToken();
    }

    public static String getToken(){
        return token;
    }
}