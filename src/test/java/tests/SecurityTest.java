package tests;

import endpoints.Orders;
import endpoints.Products;
import enums.UserRole;
import io.restassured.response.Response;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.logging.log4j.core.config.Order;
import org.testng.annotations.Test;
import payloads.request.ProductsPOJO;
import testBase.BaseClass;
import utilities.TestContext;
import utilities.TokenManager;

public class SecurityTest extends BaseClass {

    @Test
    public void OrderWithoutToken(){

        Response response = Orders.getOrders(null);

        response.then().spec(fail401());


    }

    @Test
    public void OrderWithoutRole(){

        Response response = Orders.getOrders(UserRole.USER);

        response.then().spec(fail403());

    }

    @Test
    public void accessWithExpiredToken(){

        String expiredToken = TokenManager.generateExpiredToken(UserRole.ADMIN);

        TestContext.addHeader("Authorization",
                "Bearer " + expiredToken);

        ProductsPOJO productsPOJO = ProductsPOJO.builder()
                .id(104)
                .price(221.10)
                .title("Samsung Galazy S20 FE ")
                .image("https://iamge.jpg")
                .category("electronics")
                .description("It is a very good flagship mobile")
                .build();

        Products.createProduct(productsPOJO,null,null,expiredToken)
                .then()
                .spec(fail403());

    }

    @Test
    public void tamperedToken(){

        String token = TokenManager.getToken(UserRole.ADMIN);

        ProductsPOJO productsPOJO = ProductsPOJO.builder()
                .id(104)
                .price(221.10)
                .title("Samsung Galazy S20 FE ")
                .image("https://iamge.jpg")
                .category("electronics")
                .description("It is a very good flagship mobile")
                .build();

        Products.createProduct(productsPOJO , null ,null, token+"abv")
                .then().spec(fail403());

    }






}
