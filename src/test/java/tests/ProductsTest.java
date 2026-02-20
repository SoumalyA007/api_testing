package tests;

import endpoints.Products;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import payloads.ProductsPOJO;
import testBase.BaseClass;
import static io.restassured.RestAssured.*;
import io.restassured.response.Response;
import testBase.TestContext;

import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;



public class ProductsTest extends BaseClass {


    @Test
    public static void getAllProductStatus(){

        Response resp = Products.getProducts();
        resp.then().spec(success200()).log().all();

    }

    @Test(dependsOnMethods = "getAllProductStatus")
    public static void checkProductsListSize(){
        Response resp = Products.getProducts();
        resp.then()
                .body("size()",not(empty()))
                .log().ifValidationFails();

    }

    @Test
    public static void testFields(){
        Response resp = Products.getProducts();
        resp.then()
                .spec(success200())
                .body("id",everyItem(greaterThan(0)))
                .body("price",everyItem(notNullValue()))
                .body("description", everyItem(not(isEmptyOrNullString())))
                .body("category",everyItem(not(isEmptyOrNullString())))
                .body("image",everyItem(
                        allOf(
                                startsWith("https://"),
                                anyOf(
                                        endsWith(".png"),
                                        endsWith(".jpg"),
                                        endsWith("jpeg"),
                                        endsWith("webp")
                                ))
                        )
                )
                .log().ifValidationFails();

    }

    @Test
    public static void getById(){

        Response products = Products.getProducts();
        List<Integer> productIds= products.then().extract().jsonPath().getList("id");

        int randId = productIds.get(new Random().nextInt(productIds.size()));



        Response resp = Products.getProduct(randId)
                .then()
                .spec(success200())
                .body("id",equalTo(randId))
                .log()
                .ifValidationFails()
                .extract()
                .response();

        String desc = resp.jsonPath().getString("description");
        System.out.println(desc);

    }

    @Test
    public static void createProductTest(){

        ProductsPOJO productsPOJO = ProductsPOJO.builder()
                .price(100)
                .description("PS1")
                .category("Gaming")
                .image("https://example.com/image.png")
                .build();

        Response resp = Products.createProduct(productsPOJO);
        resp
                .then()
                .spec(success201())
                .log().all();

        TestContext.productId = resp.path("id");

    }

    @Test(dependsOnMethods = "createProductTest")
    public static void updateProductTest(){

        ProductsPOJO productsPOJO = ProductsPOJO.builder()
                .title("Gaming Dude")
                .description("PS1")
                .price(1000)
                .category("Gaming")
                .image("https://example.com/image.png")
                .build();

        Response resp = Products.updateProduct(TestContext.productId , productsPOJO);
        resp.then().spec(success200()).log().all();
        resp.prettyPrint();


    }

    @Test
    public static void deleteProductTest(){
        Response resp = Products.deleteProduct(TestContext.productId);
        resp.then().spec(success200());
    }

    @Test
    public static void verifyNumericPriceTest(){

        Response resp = Products.getProducts();

        resp.then()
                .body("price", everyItem(instanceOf(Number.class)
                ));

    }

    @Test
    public static void getProductByInvalidId(){
        Response products = Products.getProducts();
        String randId = "19";


        Response resp = Products.getProduct(randId);
        resp
                .then()
                .spec(success200())
                .log()
                .ifValidationFails();

        resp.prettyPrint();

    }









}
