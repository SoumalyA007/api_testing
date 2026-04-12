package dataproviders;

import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.DataProvider;
import testBase.BaseClass;

public class OrdersDataProvider {

    @DataProvider(name = "invalidOrderPayloads")
    public Object[][] invalidOrderPayloads() {

        ResponseSpecification spec = BaseClass.fail400();

        return new Object[][]{

                {"Negative Quantity",4, 101, -1, spec},
                {"Invalid Product ID",4, Integer.MAX_VALUE, 1, spec},
                {"Negative Product ID",4, -100, 1, spec},
                {"Empty Item List",4, null, null, spec}
        };
    }
}