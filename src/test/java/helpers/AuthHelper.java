package helpers;

import endpoints.Auth;
import io.restassured.response.Response;
import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;

public class AuthHelper {

    public static String getToken(LoginRequestPOJO payload) {
        Response response = Auth.login(payload);

        LoginResponsePOJO loginResponse = response.as(LoginResponsePOJO.class);

        return loginResponse.getToken();
    }
}