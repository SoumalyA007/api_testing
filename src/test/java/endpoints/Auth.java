package endpoints;

import io.restassured.response.Response;
import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Auth {

    public static Response login(LoginRequestPOJO request){

        var req = given()
                .spec(BaseClass.get(null))
                .basePath("/auth/login");

        if(request != null){
            req.body(request);
        }

        return req
                .when()
                .post();
    }

    // generic login (for injection / malformed payloads)
    public static Response login(Object payload){

        return given()
                .spec(BaseClass.get(null))
                .basePath("/login")
                .body(payload)
                .when()
                .post();
    }

}