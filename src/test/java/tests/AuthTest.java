package tests;

import endpoints.Auth;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;
import testBase.BaseClass;
import utilities.TestContext;
import utilities.TokenManager;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class AuthTest extends BaseClass {


    @Test
    public static void loginAdmin(){
        LoginRequestPOJO loginRequestPOJO = LoginRequestPOJO.builder()
                .username("admin1")
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
                .username("testuser")
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
                .username("testuser")
                .password("passwword1234")
                .build();

        Response resp = Auth.login(loginRequestPOJO);

        resp.then().spec(fail401());


    }

    @Test
    public void loginUserEmptyBody(){

        Response resp = given()
                .spec(BaseClass.get(null))
                .basePath("/auth/login")
                .when()
                .post();

        resp.then()
                .statusCode(400); // or expected code
    }

    @Test
    public void loginWithMissingPassword(){
        LoginRequestPOJO loginRequestPOJO = LoginRequestPOJO.builder()
                .username("testuser")
                .build();
        Response resp = Auth.login(loginRequestPOJO);

        resp.then().spec(fail401());

    }

    @Test
    public static void loginSqlInjection(){

        LoginRequestPOJO loginRequestPOJO = LoginRequestPOJO.builder()
                .username("' OR 1=1 --")
                .password("' OR 1=1 --")
                .build();

        Response resp = Auth.login(loginRequestPOJO);

        resp.then().spec(fail401());


    }

    @Test
    public static void loginJSInjection(){

        LoginRequestPOJO loginRequestPOJO = LoginRequestPOJO.builder()
                .username("<script>alert(1)</script>")
                .password("pass")
                .build();

        Response resp = Auth.login(loginRequestPOJO);

        resp.then().spec(fail401());


    }

    @Test
    public void loginJSONInjection(){

        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> injection = new HashMap<>();
        injection.put("$ne", null);

        payload.put("username", injection);
        payload.put("password", injection);

        Response resp = given()
                .spec(BaseClass.get(null))
                .basePath("/login")
                .body(payload)
                .when()
                .post();

        resp.then().spec(fail401());
    }

    @Test
    public void loginWrongContentType(){

        TestContext.addHeader("Content-Type","Application/XML");

        LoginRequestPOJO loginRequestPOJO = LoginRequestPOJO.builder()
                .username("tester@qa.com")
                .password("passwword1234")
                .build();

        Response resp = Auth.login(loginRequestPOJO);

        resp.then().spec(fail415());
    }








}
