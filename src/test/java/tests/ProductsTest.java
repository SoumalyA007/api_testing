package tests;

import endpoints.Categories;
import endpoints.Products;
import io.restassured.response.Response;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.testng.annotations.Test;
import payloads.request.ProductsPOJO;
import testBase.BaseClass;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.*;



public class ProductsTest extends BaseClass {


    @Test
    public void getAllProductsTest(){
        Response resp = Products.getAllProducts();
        resp.then().spec(BaseClass.success200());

    }

    @Test
    public void verifyProductFields(){
        Response resp = Products.getAllProducts();
        resp.then()
                .spec(success200())
                .body("id",everyItem(greaterThan(0)))
                .body("title",everyItem(notNullValue()))
                .body("price",everyItem(greaterThan(0.0)))
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
    public static void categoryExistsTest(){

        List<String> categories = Categories.getCategories().then()
                .spec(success200())
                .extract()
                .jsonPath()
                .getList("name", String.class);

        Set<String> categoriesSet = new HashSet<>(categories);

        Products.getAllProducts()
                .then()
                .spec(success200())
                .body("category",everyItem(isIn(categoriesSet)));
    }

    @Test
    public static void getProductById(){

        List<Integer> ids = Products.getAllProducts().then().extract().jsonPath().getList("id", Integer.class);

        int randId = ids.get(new Random().nextInt(ids.size()));

        Products.getProductById(randId).then()
                .spec(success200())
                .body("id",equalTo(randId));

    }






}
