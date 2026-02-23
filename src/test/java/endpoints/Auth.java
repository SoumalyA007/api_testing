package endpoints;

import io.restassured.response.Response;
import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Auth {

    public static LoginResponsePOJO login(LoginRequestPOJO request){

        Response response = given()
                .spec(BaseClass.get())
                .basePath("/auth/login")
                .body(request)
                .when()
                .post();

        return response.as(LoginResponsePOJO.class);
    }
}