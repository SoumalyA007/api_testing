package dataproviders;

import enums.UserRole;
import helpers.ProductHelper;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.DataProvider;
import testBase.BaseClass;

public class ProductDataProvider {

    @DataProvider(name = "invalidProductPayloads")
    public Object[][] invalidProductPayloads() {
        return new Object[][]{

                {ProductHelper.negativePriceProduct()},
                {ProductHelper.productWithoutTitle()}

        };
    }

    @DataProvider(name = "updateOrCreateProductPayloads")
    public Object[][] updateOrCreateProductPayloads() {
        return new Object[][]{

                {"Create as ADMIN",ProductHelper.validProduct(), UserRole.ADMIN, BaseClass.success201()},
                {"Create as USER",ProductHelper.validProduct(), UserRole.USER,BaseClass.fail403()},
                {"Create invalid data(negative price)",ProductHelper.negativePriceProduct(),UserRole.ADMIN,BaseClass.fail400()},
                {"Create invalid data(without title)",ProductHelper.productWithoutTitle(),UserRole.ADMIN,BaseClass.fail400()}

        };
    }

    @DataProvider(name = "deleteProduct")
    public Object[][] deleteProductPayloads() {
        return new Object[][]{

                {"Delete as ADMIN", UserRole.ADMIN, BaseClass.success200()},
                {"Update as USER", UserRole.USER,BaseClass.fail403()},
                {"Updating with invalid Id", UserRole.ADMIN,BaseClass.fail403()}


        };
    }




}