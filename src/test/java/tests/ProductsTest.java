package tests;

import dataproviders.ProductDataProvider;
import endpoints.Categories;
import endpoints.Products;
import enums.UserRole;
import helpers.ProductHelper;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.Test;
import payloads.request.ProductsPOJO;
import testBase.BaseClass;
import utilities.TestContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.Matchers.*;

public class ProductsTest extends BaseClass {

    @Test
    public void getAllProductsTest() {
        Products.getAllProducts(null)
        .then().spec(success200());
    }

    @Test
    public  void getProductById() {

        int randId = ProductHelper.getRandomProductId();

        Products.getProductById(randId,null).then()
                .spec(success200())
                .body("id", equalTo(randId));

    }

    @Test
    public  void getProductByCategory() {



        String randCategory = ProductHelper.getRandomCategory();

        Products.getProductsByCategory(randCategory, null)
                .then().spec(success200())
                .body("id", notNullValue());

    }

    @Test
    public void getProductByInvalidId(){

        int createdId = Integer.MAX_VALUE;

        Products.getProductById(createdId,null).then()
                .spec(fail404())
                .body("id", equalTo(createdId));

    }

    @Test
    public void getProductByInvalidCategory() {

        String randCategory = ProductHelper.getRandomCategory() + ProductHelper.getRandomCategory();

        Products.getProductsByCategory(randCategory, null)
                .then().spec(fail400());

    }

    @Test
    public void verifyProductFields() {
        Products.getAllProducts(null)
                .then()
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
    public void categoryExistsTest() {

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


    @Test(dataProvider = "updateOrCreateProductPayloads", dataProviderClass = ProductDataProvider.class)
    public  void createProduct(String message, ProductsPOJO payload, UserRole role, ResponseSpecification resp){


        System.out.println("The current test is :- "+message);

        Response product = Products.createProduct(payload, role);
        product.then().spec(resp);

        if(product.statusCode()==200){
            int createdId = product.then().extract().jsonPath().getInt("id");
            Products.deleteProduct(createdId,UserRole.ADMIN);
        }


    }


    @Test
    public void createProductPriceAsString() {

        Products.createProduct(ProductHelper.productPriceAsString(), UserRole.ADMIN).then().spec(fail400());
    }

    @Test
    public void createProductXSSInDescription() {

        Products.createProduct(ProductHelper.xssProduct(), UserRole.ADMIN).then().spec(fail400());
    }


    @Test(dataProvider = "updateOrCreateProductPayloads", dataProviderClass = ProductDataProvider.class)
    public void updateProducts(String message, ProductsPOJO payload, UserRole role, ResponseSpecification resp){

        int createdId = ProductHelper.createTestProduct();
        System.out.println("Test name :-- " +  message);
        Products.updateProduct(createdId,payload,role)
                .then()
                .spec(resp);
        Products.deleteProduct(createdId,UserRole.ADMIN);


    }

    @Test
    public void updateNonExistingProduct(){

        int updateId =  Integer.MAX_VALUE;
        Products.updateProduct(updateId , ProductHelper.validProduct(), UserRole.ADMIN)
                .then()
                .spec(fail404());

    }

    @Test(dataProvider = "deleteProduct", dataProviderClass = ProductDataProvider.class)
    public void deleteProductWithAdmin(String message, UserRole role, ResponseSpecification spec){

        int createdId = ProductHelper.createTestProduct();

        Products.deleteProduct(createdId,role).then().spec(spec);

    }

    @Test
    public void deleteProductWithInvalidId(){

        int deleteId =  Integer.MAX_VALUE;

        Products.deleteProduct(deleteId,UserRole.ADMIN).then().spec(fail404());


    }

}
