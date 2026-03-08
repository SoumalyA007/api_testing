package tests;

import dataproviders.ProductDataProvider;
import endpoints.Categories;
import endpoints.Products;
import enums.UserRole;
import helpers.ProductHelper;
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

        int invalidId = ProductHelper.getLastProductId() + Integer.MAX_VALUE;

        Products.getProductById(invalidId,null).then()
                .spec(fail404())
                .body("id", equalTo(invalidId));

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


    @Test
    public  void createProductWithAdmin(){

        Products.createProduct(ProductHelper.validProduct(), UserRole.ADMIN)
                .then()
                .spec(fail403());

    }

    @Test
    public  void createProductWithoutAdmin() {

        Products.createProduct(ProductHelper.validProduct(),UserRole.USER)
                .then()
                .spec(fail403());


    }

    @Test(dataProvider = "invalidProductPayloads",
            dataProviderClass = ProductDataProvider.class)
    public void createProductInvalidPayloads(ProductsPOJO payload){

        Products.createProduct(payload, UserRole.ADMIN)
                .then()
                .spec(fail400());

    }

    @Test
    public void createProductPriceAsString() {

        Products.createProduct(ProductHelper.productPriceAsString(), UserRole.ADMIN).then().spec(fail400());
    }

    @Test
    public void createProductXSSInDescription() {

        Products.createProduct(ProductHelper.xssProduct(), UserRole.ADMIN).then().spec(fail400());
    }


    @Test
    public void updateWithAdmin(){

        Products.updateProduct(ProductHelper.getLastProductId() , ProductHelper.validProduct(), UserRole.ADMIN)
                .then()
                .spec(success200());

    }

    @Test
    public void updateWithoutAdmin(){

        Products.updateProduct(ProductHelper.getLastProductId() , ProductHelper.validProduct(), UserRole.USER)
                .then()
                .spec(fail403());

    }

    @Test
    public void updateNonExistingProduct(){

        int updateId = ProductHelper.getLastProductId()+ Integer.MAX_VALUE;
        Products.updateProduct(updateId , ProductHelper.validProduct(), UserRole.ADMIN)
                .then()
                .spec(fail404());

    }



    @Test
    public void deleteProductWithAdmin(){

        Products.deleteProduct(ProductHelper.getLastProductId(),UserRole.ADMIN).then().spec(success200());

    }

    @Test
    public void deleteProductWithoutAdmin(){

        Products.deleteProduct(ProductHelper.getLastProductId(),UserRole.USER).then().spec(fail403());

    }

    @Test
    public void deleteProductWithInvalidId(){

        int deleteId = ProductHelper.getLastProductId() + Integer.MAX_VALUE;

        Products.deleteProduct(deleteId,UserRole.ADMIN).then().spec(fail404());


    }

}
