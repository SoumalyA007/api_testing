package dataproviders;

import helpers.AuthHelper;
import org.testng.annotations.DataProvider;

public class AuthDataProvider {

    @DataProvider(name = "invalidLoginPayloads")
    public Object[][] invalidLoginPayloads() {

        return new Object[][]{

                // Missing password
                {AuthHelper.loginWithoutPassword("admin")},

                // Missing username
                {AuthHelper.loginWithoutUsername("password123")},

                // Null password
                {AuthHelper.loginasUserOrAdmin("admin1",null)},

                // Null username
                {AuthHelper.loginasUserOrAdmin(null,"password123")},

                // Empty username
                {AuthHelper.loginasUserOrAdmin("","password123")},

                // Empty password
                {AuthHelper.loginasUserOrAdmin("admin1","")},

                //both null values
                {AuthHelper.loginasUserOrAdmin(null,null)},

                //Both empty values
                {AuthHelper.loginasUserOrAdmin("","")}
        };
    }
}