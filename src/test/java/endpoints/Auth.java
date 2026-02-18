package endpoints;

import io.restassured.response.Response;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Auth {

    public static Response login(){

        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/auth/login")
                .when()
                .post();

        return resp;

    }
}
