package tests;

import endpoints.Products;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import payloads.ProductsPOJO;
import testBase.BaseClass;
import static io.restassured.RestAssured.*;
import io.restassured.response.Response;

import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;



public class ProductsTest extends BaseClass {


    @Test
    public static void getAllProductStatus(){

        Response resp = Products.getProducts();
        resp.then().spec(BaseClass.success200()).log().all();

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
                .spec(BaseClass.success200())
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
                .spec(BaseClass.success200())
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

        Products.createProduct(productsPOJO)
                .then()
                .spec(BaseClass.success201())
                .log().all();


    }




}
