package tests;

import endpoints.Categories;
import endpoints.Products;
import endpoints.Users;
import enums.UserRole;
import io.restassured.response.Response;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.testng.annotations.Test;
import payloads.request.ProductsPOJO;
import testBase.BaseClass;
import utilities.TestContext;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.*;



public class ProductsTest extends BaseClass {


    @Test
    public void getAllProductsTest() {
        Response resp = Products.getAllProducts(null);
        resp.then().spec(BaseClass.success200());

    }

    @Test
    public void verifyProductFields() {
        Response resp = Products.getAllProducts(null);
        resp.then()
                .spec(success200())
                .body("id", everyItem(greaterThan(0)))
                .body("title", everyItem(notNullValue()))
                .body("price", everyItem(greaterThan(0.0)))
                .body("description", everyItem(not(isEmptyOrNullString())))
                .body("category", everyItem(not(isEmptyOrNullString())))
                .body("image", everyItem(
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
    public static void categoryExistsTest() {

        List<String> categories = Categories.getCategories(null).then()
                .spec(success200())
                .extract()
                .jsonPath()
                .getList("name", String.class);

        Set<String> categoriesSet = new HashSet<>(categories);

        TestContext.set("categories", categories);

        Products.getAllProducts(null)
                .then()
                .spec(success200())
                .body("category", everyItem(isIn(categoriesSet)));
    }

    @Test
    public static void getProductById() {

        List<Integer> ids = Products.getAllProducts(null).then().extract().jsonPath().getList("id", Integer.class);

        int randId = ids.get(new Random().nextInt(ids.size()));

        Products.getProductById(randId,null).then()
                .spec(success200())
                .body("id", equalTo(randId));

    }

    @Test
    public static void getProductByCategory() {

        List<String> categories = (List<String>) TestContext.get("categories");

        String randCategory = categories.get(new Random().nextInt(categories.size()));

        Products.getProductsByCategory(randCategory, null)
                .then().spec(success200())
                .body("id", notNullValue());

    }

    @Test
    public static void createProductWithoutAdmin() {

        ProductsPOJO productsPOJO = ProductsPOJO.builder()
                .id(104)
                .price(221.10)
                .title("Samsung Galazy S20 FE ")
                .image("https://iamge.jpg")
                .category("electronics")
                .description("It is a very good flagship mobile")
                .build();

        Products.createProduct(productsPOJO,null, null,null)
                .then()
                .spec(fail403());


    }

    @Test
    public static void createProductWithAdmin(){
        ProductsPOJO productsPOJO = ProductsPOJO.builder()
                .id(104)
                .price(221.10)
                .title("Samsung Galazy S20 FE ")
                .image("https://iamge.jpg")
                .category("electronics")
                .description("It is a very good flagship mobile")
                .build();

        Products.createProduct(productsPOJO, UserRole.ADMIN,null,null)
                .then()
                .spec(fail403());

    }

    @Test
    public static void updateWithAdmin(){
        ProductsPOJO productsPOJO = ProductsPOJO.builder()

                .price(221.10)
                .title("Samsung Galazy S20 FE ")
                .image("https://iamge.jpg")
                .category("electronics")
                .description("It is a very good flagship mobile")
                .build();

        Response resp = Products.getAllProducts(UserRole.USER);
        List<Integer> productIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int updateId = productIds.get(productIds.size()-1);

        Products.updateProduct(updateId , productsPOJO, UserRole.ADMIN)
                .then()
                .spec(success200());

    }

    @Test
    public static void updateWithoutAdmin(){
        ProductsPOJO productsPOJO = ProductsPOJO.builder()

                .price(221.10)
                .title("Samsung Galazy S20 FE ")
                .image("https://iamge.jpg")
                .category("electronics")
                .description("It is a very good flagship mobile")
                .build();

        Response resp = Products.getAllProducts(UserRole.USER);
        List<Integer> productIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int updateId = productIds.get(productIds.size()-1);

        Products.updateProduct(updateId , productsPOJO, UserRole.USER)
                .then()
                .spec(fail403());

    }

    @Test
    public void deleteProductWithAdmin(){
        Response resp = Products.getAllProducts(UserRole.USER);
        List<Integer> productIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int deleteId = productIds.get(productIds.size()-1);

        Products.deleteProduct(deleteId,UserRole.ADMIN).then().spec(success200());

    }

    @Test
    public void deleteProductWithOutAdmin(){
        Response resp = Products.getAllProducts(UserRole.USER);
        List<Integer> productIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int deleteId = productIds.get(productIds.size()-1);

        Products.deleteProduct(deleteId,UserRole.ADMIN).then().spec(success200());

    }


    @Test
    public void getProductByInvalidId(){

        Response resp = Products.getAllProducts(UserRole.USER);
        List<Integer> productIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int invalidId = productIds.get(productIds.size()-1);

        Products.getProductById(invalidId,null).then()
                .spec(fail404())
                .body("id", equalTo(invalidId));

    }

    @Test
    public static void createProductWithNegativePrice(){
        ProductsPOJO productsPOJO = ProductsPOJO.builder()
                .price(-221.10)
                .title("Samsung Galazy S20 FE ")
                .image("https://iamge.jpg")
                .category("electronics")
                .description("It is a very good flagship mobile")
                .build();

        Products.createProduct(productsPOJO, UserRole.ADMIN,null,null)
                .then()
                .spec(fail400());

    }

    @Test
    public static void createProductWithOutTitle(){
        ProductsPOJO productsPOJO = ProductsPOJO.builder()
                .price(-221.10)
                .image("https://iamge.jpg")
                .category("electronics")
                .description("It is a very good flagship mobile")
                .build();

        Products.createProduct(productsPOJO, UserRole.ADMIN,null,null)
                .then()
                .spec(fail400());

    }

    @Test
    public static void updateNonExistingProduct(){
        ProductsPOJO productsPOJO = ProductsPOJO.builder()

                .price(221.10)
                .title("Samsung Galazy S20 FE ")
                .image("https://iamge.jpg")
                .category("electronics")
                .description("It is a very good flagship mobile")
                .build();

        Response resp = Products.getAllProducts(UserRole.USER);
        List<Integer> productIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int updateId = productIds.get(productIds.size()+1);

        Products.updateProduct(updateId , productsPOJO, UserRole.ADMIN)
                .then()
                .spec(fail404());

    }

    @Test
    public void deleteProductWithInvalidId(){
        Response resp = Products.getAllProducts(UserRole.USER);
        List<Integer> productIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int deleteId = productIds.get(productIds.size()+1);

        Products.deleteProduct(deleteId,UserRole.ADMIN).then().spec(fail404());

    }

    @Test
    public static void getProductByInvalidCategory() {

        List<String> categories = Categories.getCategories(UserRole.USER).then().extract().jsonPath().getList("name");

        String randCategory = categories.get(new Random().nextInt(categories.size())) + categories.get(new Random().nextInt(categories.size()));

        Products.getProductsByCategory(randCategory, null)
                .then().spec(success200())
                .body("id", notNullValue());

    }

    @Test
    public void createProductVeryLargePrice() {

        ProductsPOJO product = ProductsPOJO.builder()
                .title("Test Product")
                .price(Double.MAX_VALUE)
                .description("Valid description")
                .build();

        Response resp = Products.createProduct(product, UserRole.ADMIN ,null,null);

        resp.then().statusCode(400);
    }

    @Test
    public void createProductPriceAsString() {

        String invalidBody = """
        {
          "title": "Test Product",
          "price": "1000",
          "description": "Test desc"
        }
        """;

        Products.createProduct(null,UserRole.ADMIN , invalidBody,null).then().spec(fail400());

    }

    @Test
    public void createProductXSSInDescription() {

        ProductsPOJO product = ProductsPOJO.builder()
                .title("Safe Product")
                .price(200)
                .description("<script>alert('XSS')</script>")
                .build();

        Response resp = Products.createProduct(product, UserRole.ADMIN,null,null);

        resp.then().statusCode(400);
    }










}
