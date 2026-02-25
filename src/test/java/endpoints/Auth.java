package endpoints;

import io.restassured.response.Response;
import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Auth {

    public static Response login(LoginRequestPOJO request){

        Response response = given()
                .spec(BaseClass.get(null))
                .basePath("/auth/login")
                .body(request)
                .when()
                .post();

        return response;

        //return response.as(LoginResponsePOJO.class);
    }
}