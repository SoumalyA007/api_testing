package tests;

import dataproviders.AuthDataProvider;
import endpoints.Auth;
import helpers.AuthHelper;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
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

        //calling login function
        LoginRequestPOJO loginRequestPOJO = AuthHelper.loginasUserOrAdmin("testuser","password123");

        Response resp = Auth.login(loginRequestPOJO);
        resp.then().spec(success200());

        LoginResponsePOJO loginResponsePOJO = resp.as(LoginResponsePOJO.class);

        Assert.assertNotNull(loginResponsePOJO.getToken(),"The token should not be null");
        Assert.assertNotNull(loginResponsePOJO.getRole(),"The role should not be null");

    }

    @Test(dataProvider = "invalidLoginPayloads", dataProviderClass = AuthDataProvider.class)
    public static void invalidLogins(String message,LoginRequestPOJO loginRequestPOJO, ResponseSpecification spec){

        Auth.login(loginRequestPOJO)
                .then()
                .spec(spec);

    }

    @Test(dataProvider = "securityPayloads", dataProviderClass = AuthDataProvider.class)
    public static void securityLoginTests(String message,LoginRequestPOJO loginRequestPOJO , ResponseSpecification spec){

        Auth.login(loginRequestPOJO).then()
                .spec(spec);

    }

    @Test
    public void loginJSONInjection(){

        Map<String,Object> payload = AuthHelper.loginJsonInjectionPayload();
        Auth.login(payload)
                .then()
                .spec(fail401());

    }

    @Test
    public void loginWrongContentType(){

        TestContext.addHeader("Content-Type","Application/XML");

        LoginRequestPOJO loginRequestPOJO = AuthHelper.loginasUserOrAdmin("testuser","passwword1234");

        Auth.login(loginRequestPOJO).then()
                .spec(fail415());

    }


}
