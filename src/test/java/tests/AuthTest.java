package tests;

import endpoints.Auth;
import helpers.AuthHelper;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;
import testBase.BaseClass;
import utilities.TestContext;
import utilities.TokenManager;
import helpers.AuthHelper;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class AuthTest extends BaseClass {


    //login as user
    @Test
    public static void loginAdmin(){

        //calling login function
        LoginRequestPOJO loginRequestPOJO = AuthHelper.loginasUserOrAdmin("admin1","password123");

        Response resp = Auth.login(loginRequestPOJO);
        resp.then().spec(success200());

        LoginResponsePOJO loginResponsePOJO = resp.as(LoginResponsePOJO.class);

        Assert.assertNotNull(loginResponsePOJO.getToken(),"The token should not be null");
        Assert.assertNotNull(loginResponsePOJO.getRole(),"The role should not be null");

    }

    //login as admin
    @Test
    public static void loginUser(){

        LoginRequestPOJO loginRequestPOJO = AuthHelper.loginasUserOrAdmin("testuser","password123");

        Response resp = Auth.login(loginRequestPOJO);
        resp.then().spec(success200());

        LoginResponsePOJO loginResponsePOJO = resp.as(LoginResponsePOJO.class);

        Assert.assertNotNull(loginResponsePOJO.getToken(),"The token should not be null");
        Assert.assertNotNull(loginResponsePOJO.getRole(),"The role should not be null");

    }

    //login with invalid credentials as user
    @Test
    public static void loginUserInvalid(){

        LoginRequestPOJO loginRequestPOJO = AuthHelper.loginasUserOrAdmin("testuser","passwword1234");

        Response resp = Auth.login(loginRequestPOJO);

        resp.then().spec(fail401());


    }

    //login with empty request body as user
    @Test
    public void loginUserEmptyBody(){

        Auth.login(null)
                .then()
                .spec(fail400());

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
