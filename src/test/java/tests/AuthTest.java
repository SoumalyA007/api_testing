package tests;

import endpoints.Auth;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;
import testBase.BaseClass;
import utilities.TokenManager;

import static io.restassured.RestAssured.given;

public class AuthTest extends BaseClass {


    @Test
    public static void loginAdmin(){
        LoginRequestPOJO loginRequestPOJO = LoginRequestPOJO.builder()
                .email("admin@enterprise.com")
                .password("password123")
                .build();

        Response resp = Auth.login(loginRequestPOJO);
        resp.then().spec(success200());

        LoginResponsePOJO loginResponsePOJO = resp.as(LoginResponsePOJO.class);

        Assert.assertNotNull(loginResponsePOJO.getToken(),"The token should not be null");
        Assert.assertNotNull(loginResponsePOJO.getRole(),"The role should not be null");

    }

    @Test
    public static void loginUser(){

        LoginRequestPOJO loginRequestPOJO = LoginRequestPOJO.builder()
                .email("tester@qa.com")
                .password("password123")
                .build();

        Response resp = Auth.login(loginRequestPOJO);
        resp.then().spec(success200());

        LoginResponsePOJO loginResponsePOJO = resp.as(LoginResponsePOJO.class);

        Assert.assertNotNull(loginResponsePOJO.getToken(),"The token should not be null");
        Assert.assertNotNull(loginResponsePOJO.getRole(),"The role should not be null");

    }

    @Test
    public static void loginUserInvalid(){

        LoginRequestPOJO loginRequestPOJO = LoginRequestPOJO.builder()
                .email("tester@qa.com")
                .password("passwword1234")
                .build();

        Response resp = Auth.login(loginRequestPOJO);

        resp.then().spec(fail401());


    }

    @Test
    public void loginUserEmptyBody(){

        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/auth/login")
                .when()
                .post();

        resp.then()
                .statusCode(400); // or expected code
    }




}
