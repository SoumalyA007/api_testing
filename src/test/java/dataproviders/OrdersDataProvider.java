package dataproviders;

import enums.UserRole;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.DataProvider;
import testBase.BaseClass;

public class OrdersDataProvider {

    @DataProvider(name = "invalidOrderPayloads")
    public Object[][] invalidOrderPayloads() {

        ResponseSpecification spec = BaseClass.fail400();

        return new Object[][]{

                {"Negative Quantity", 101, -1, spec},
                {"Invalid Product ID", Integer.MAX_VALUE, 1, spec},
                {"Negative Product ID", -100, 1, spec},
                {"Empty Item List", null, null, spec}
        };
    }
}