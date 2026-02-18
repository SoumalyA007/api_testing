package tests;

import endpoints.Products;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import testBase.BaseClass;
import static io.restassured.RestAssured.*;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.*;



public class ProductsTest extends BaseClass {


    @Test
    public static void getAllProductStatus(){

        Response resp = Products.get_all_products();
        resp.then().spec(BaseClass.success200()).log().all();

    }

    @Test(dependsOnMethods = "getAllProductStatus")
    public static void checkProductsListSize(){
        Response resp = Products.get_all_products();
        resp.then()
                .body("size()",not(empty()))
                .log().ifValidationFails();

    }

    @Test
    public static void testFields(){
        Response resp = Products.get_all_products();
        resp.then()
                .spec(BaseClass.success200())
                .body("id",everyItem(greaterThan(0)))
                .body("price",greaterThan(0f))
                .body("description", not(empty()))
                .body("category",not(empty()))
                .body("image",startsWith("https://"),
                        anyOf(
                                endsWith(".png"),
                                endsWith(".jpg"),
                                endsWith("jpeg"),
                                endsWith("webp")
                        ))
                .log().ifValidationFails();

    }





}
