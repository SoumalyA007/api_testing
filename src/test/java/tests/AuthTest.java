package tests;

import dataproviders.AuthDataProvider;
import endpoints.Auth;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.LoginRequestPOJO;
import payloads.response.LoginResponsePOJO;
import testBase.BaseClass;
import testData.AuthTestDataFactory;
import utilities.TestContext;

import java.util.Map;

public class AuthTest extends BaseClass {

    // ================= POSITIVE =================

    @Test(groups = {"smoke", "auth"}, priority = 1)
    public void loginAdmin() {

        LoginRequestPOJO payload =
                AuthTestDataFactory.validLogin("admin1", "password123");

        Response response = Auth.login(payload);
        response.then().spec(success200());

        LoginResponsePOJO loginResponse = response.as(LoginResponsePOJO.class);

        Assert.assertNotNull(loginResponse.getToken(), "Token should not be null");
        Assert.assertNotNull(loginResponse.getRole(), "Role should not be null");
    }

    @Test(groups = {"smoke", "auth"}, priority = 2)
    public void loginUser() {

        LoginRequestPOJO payload =
                AuthTestDataFactory.validLogin("testuser", "password123");

        Response response = Auth.login(payload);
        response.then().spec(success200());

        LoginResponsePOJO loginResponse = response.as(LoginResponsePOJO.class);

        Assert.assertNotNull(loginResponse.getToken(), "Token should not be null");
        Assert.assertNotNull(loginResponse.getRole(), "Role should not be null");
    }

    // ================= NEGATIVE =================

    @Test(
        dataProvider = "invalidLoginPayloads",
        dataProviderClass = AuthDataProvider.class,
        groups = {"negative", "auth"},
        priority = 3
      )
    public void invalidLogins(String message, LoginRequestPOJO payload, ResponseSpecification spec) {

        System.out.println("Test: " + message);

        Auth.login(payload)
                .then()
                .spec(spec);
    }

    // ================= SECURITY =================

    @Test(
        dataProvider = "securityPayloads",
        dataProviderClass = AuthDataProvider.class,
        groups = {"security", "auth"},
        priority = 4
      )
    public void securityLoginTests(String message, LoginRequestPOJO payload, ResponseSpecification spec) {

        System.out.println("Test: " + message);

        Auth.login(payload)
                .then()
                .spec(spec);
    }

    @Test(groups = {"security", "negative", "auth"}, priority = 5)
    public void loginJSONInjection() {

        Map<String, Object> payload =
                AuthTestDataFactory.jsonInjectionPayload();

        Auth.login(payload)
                .then()
                .spec(fail401());
    }

    // ================= HEADERS =================

    @Test(groups = {"negative", "auth"}, priority = 6)
    public void loginWrongContentType() {

        TestContext.addHeader("Content-Type", "application/xml");

        LoginRequestPOJO payload =
                AuthTestDataFactory.validLogin("testuser", "password123");

        Auth.login(payload)
                .then()
                .spec(fail415());
    }
}