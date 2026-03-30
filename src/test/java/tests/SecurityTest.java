package tests;

import endpoints.Orders;
import endpoints.Products;
import enums.UserRole;
import helpers.ProductHelper;
import org.testng.annotations.Test;
import payloads.request.ProductsPOJO;
import testBase.BaseClass;
import testData.ProductTestDataFactory;
import utilities.TokenManager;

public class SecurityTest extends BaseClass {

    @Test(groups = {"security", "smoke"})
    public void OrderWithoutToken(){

        Orders.getOrdersWithOutAuth().then().spec(fail401());

    }

    @Test(groups = {"security"})
    public void OrderWithoutRole(){

        Orders.getOrders(UserRole.USER).then().spec(fail403());

    }

    @Test(groups = {"security"})
    public void accessWithExpiredToken(){

        String expiredToken = TokenManager.generateExpiredToken(UserRole.ADMIN);

        ProductsPOJO productsPOJO = ProductTestDataFactory.validProduct();

        Products.createProduct(productsPOJO,expiredToken)
                .then()
                .spec(fail403());

    }

    @Test(groups = {"security", "negative"})
    public void tamperedToken(){

        String token = TokenManager.getToken(UserRole.ADMIN);

        ProductsPOJO productsPOJO = ProductTestDataFactory.validProduct();

        Products.createProduct(productsPOJO , token+"abv")
                .then().spec(fail403());

    }


}