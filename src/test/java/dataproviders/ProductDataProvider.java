package dataproviders;

import enums.UserRole;
import helpers.ProductHelper;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.DataProvider;
import testBase.BaseClass;
import testData.ProductTestDataFactory;

public class ProductDataProvider {

    @DataProvider(name = "createProductPayloads")
    public Object[][] createProductPayloads() {
        return new Object[][]{

                {"Create as ADMIN", ProductTestDataFactory.validProduct(), UserRole.ADMIN, BaseClass.success201()},
                {"Create as USER", ProductTestDataFactory.validProduct(), UserRole.USER, BaseClass.fail403()},
                {"Negative price", ProductTestDataFactory.negativePriceProduct(), UserRole.ADMIN, BaseClass.fail400()},
                {"Missing title", ProductTestDataFactory.productWithoutTitle(), UserRole.ADMIN, BaseClass.fail400()}
        };
    }

    @DataProvider(name = "updateProductPayloads")
    public Object[][] updateProductPayloads() {
        return new Object[][]{

                {"Update as ADMIN", ProductTestDataFactory.validProduct(), UserRole.ADMIN, BaseClass.success200()},
                {"Update as USER", ProductTestDataFactory.validProduct(), UserRole.USER, BaseClass.fail403()},
                {"Invalid price update", ProductTestDataFactory.negativePriceProduct(), UserRole.ADMIN, BaseClass.fail400()}
        };
    }

    @DataProvider(name = "deleteProduct")
    public Object[][] deleteProductPayloads() {
        return new Object[][]{

                {"Delete as ADMIN", UserRole.ADMIN, BaseClass.success200()},
                {"Delete as USER", UserRole.USER, BaseClass.fail403()}
        };
    }

    @DataProvider(name = "invalidProductPayloads")
    public Object[][] invalidProductPayloads() {
        return new Object[][]{

                {ProductTestDataFactory.negativePriceProduct()},
                {ProductTestDataFactory.productWithoutTitle()}
        };
    }
}