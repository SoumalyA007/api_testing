package dataproviders;

import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.DataProvider;
import testBase.BaseClass;
import testData.AuthTestDataFactory;

public class AuthDataProvider {

    ResponseSpecification badRequest = BaseClass.fail400();
    ResponseSpecification unauthorizedRequest = BaseClass.fail401();

    @DataProvider(name = "invalidLoginPayloads",parallel = true)
    public Object[][] invalidLoginPayloads() {

        return new Object[][]{

                {"Missing Password", AuthTestDataFactory.loginWithoutPassword("admin"), badRequest},
                {"Missing Username", AuthTestDataFactory.loginWithoutUsername("password123"), badRequest},
                {"Null Password", AuthTestDataFactory.validLogin("admin1", null), badRequest},
                {"Null Username", AuthTestDataFactory.validLogin(null, "password123"), badRequest},
                {"Empty username", AuthTestDataFactory.validLogin("", "password123"), badRequest},
                {"Empty password", AuthTestDataFactory.validLogin("admin1", ""), badRequest},
                {"Both null", AuthTestDataFactory.validLogin(null, null), badRequest},
                {"Both empty", AuthTestDataFactory.validLogin("", ""), badRequest}
        };
    }

    @DataProvider(name = "securityPayloads",parallel = true)
    public Object[][] securityPayloads() {

        return new Object[][]{

                {"SQL Injection", AuthTestDataFactory.sqlInjectionPayload(), unauthorizedRequest},
                {"XSS Injection", AuthTestDataFactory.xssPayload(), unauthorizedRequest},
                {"JSON Injection", AuthTestDataFactory.jsonInjectionPayload(), unauthorizedRequest}
        };
    }
}