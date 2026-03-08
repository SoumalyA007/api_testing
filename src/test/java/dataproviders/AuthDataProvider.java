package dataproviders;

import helpers.AuthHelper;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.DataProvider;
import testBase.BaseClass;

public class AuthDataProvider {

    ResponseSpecification badRequest = BaseClass.fail400();
    ResponseSpecification unauthorizedRequest = BaseClass.fail401();

    @DataProvider(name = "invalidLoginPayloads")
    public Object[][] invalidLoginPayloads() {

        return new Object[][]{

                // Missing password
                {"Missing Password",AuthHelper.loginWithoutPassword("admin"), badRequest},

                // Missing username
                {"Missing Username",AuthHelper.loginWithoutUsername("password123"),badRequest},

                // Null password
                {"Null Password",AuthHelper.loginasUserOrAdmin("admin1",null),badRequest},

                // Null username
                {"Null Username",AuthHelper.loginasUserOrAdmin(null,"password123"),badRequest},

                // Empty username
                {"Empty username",AuthHelper.loginasUserOrAdmin("","password123"),badRequest},

                // Empty password
                {"Empty password",AuthHelper.loginasUserOrAdmin("admin1",""),badRequest},

                //both null values
                {"Both null values",AuthHelper.loginasUserOrAdmin(null,null),badRequest},

                //Both empty values
                {"Both empty values",AuthHelper.loginasUserOrAdmin("",""),badRequest}
        };
    }



    @DataProvider(name = "securityPayloads")
    public Object[][] securityPayloads() {

        return new Object[][]{

                // Missing password
                {"SQL Injection",AuthHelper.loginasUserOrAdmin("' OR 1=1 --","' OR 1=1 --"),unauthorizedRequest},
                {"JS Injection",AuthHelper.loginasUserOrAdmin("<script>alert(1)</script>","password123"),unauthorizedRequest},
                {"JS Injection",AuthHelper.loginasUserOrAdmin("<script>alert(1)</script>","password123"),unauthorizedRequest},
                {"JSON Injection - $ne",AuthHelper.loginJsonInjectionPayload(),unauthorizedRequest}

        };
    }
}