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
import testData.ProductTestDataFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;

public class ProductsTest extends BaseClass {

    @Test(groups = {"smoke", "products"})
    public void getAllProductsTest() {
        Products.getAllProducts(null)
                .then()
                .spec(success200());
    }

    @Test(groups = {"smoke", "products"})
    public void getProductById() {

        int randId = ProductHelper.getRandomProductId(UserRole.USER);

        Products.getProductById(randId, null)
                .then()
                .spec(success200())
                .body("id", equalTo(randId));
    }

    @Test
    public void getProductByCategory() {

        String randCategory = ProductHelper.getRandomCategory();

        Products.getProductsByCategory(randCategory, null)
                .then()
                .spec(success200())
                .body("id", notNullValue());
    }

    @Test
    public void getProductByInvalidId() {

        int invalidId = 9999999;

        Products.getProductById(invalidId, null)
                .then()
                .spec(fail404());
    }

    @Test
    public void getProductByInvalidCategory() {

        String invalidCategory = "invalid_category";

        Products.getProductsByCategory(invalidCategory, null)
                .then()
                .spec(fail400());
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
                .body("image", everyItem(startsWith("https://")));
    }

    @Test
    public void categoryExistsTest() {

        List<String> categories = Categories.getCategories(null)
                .then()
                .spec(success200())
                .extract()
                .jsonPath()
                .getList("name", String.class);

        Set<String> categorySet = new HashSet<>(categories);

        Products.getAllProducts(null)
                .then()
                .spec(success200())
                .body("category", everyItem(isIn(categorySet)));
    }

    // ================= CREATE =================

    @Test(dataProvider = "createProductPayloads", dataProviderClass = ProductDataProvider.class)
    public void createProduct(String message, ProductsPOJO payload, UserRole role, ResponseSpecification spec) {

        System.out.println("Test: " + message);

        Response response = Products.createProduct(payload, role);
        response.then().spec(spec);

        if (response.statusCode() == 201) {
            int createdId = response.then().extract().path("id");
            Products.deleteProduct(createdId, UserRole.ADMIN);
        }
    }

    @Test
    public void createProductPriceAsString() {

        Products.createProduct(ProductTestDataFactory.productPriceAsString(), UserRole.ADMIN)
                .then()
                .spec(fail400());
    }

    @Test
    public void createProductXSSInDescription() {

        Products.createProduct(ProductTestDataFactory.xssProduct(), UserRole.ADMIN)
                .then()
                .spec(fail400());
    }

    // ================= UPDATE =================

    @Test(dataProvider = "updateProductPayloads", dataProviderClass = ProductDataProvider.class)
    public void updateProducts(String message, ProductsPOJO payload, UserRole role, ResponseSpecification spec) {

        int createdId = ProductHelper.createTestProduct();

        System.out.println("Test: " + message);

        Response response = Products.updateProduct(createdId, payload, role);
        response.then().spec(spec);

        if (response.statusCode() == 200) {
            Products.getProductById(createdId, null)
                    .then()
                    .body("title", equalTo(payload.getTitle()));
        }

        Products.deleteProduct(createdId, UserRole.ADMIN);
    }

    @Test
    public void updateNonExistingProduct() {

        int invalidId = 9999999;

        Products.updateProduct(invalidId, ProductTestDataFactory.validProduct(), UserRole.ADMIN)
                .then()
                .spec(fail404());
    }

    // ================= DELETE =================

    @Test(dataProvider = "deleteProduct", dataProviderClass = ProductDataProvider.class)
    public void deleteProductTest(String message, UserRole role, ResponseSpecification spec) {

        int createdId = ProductHelper.createTestProduct();

        System.out.println("Test: " + message);

        Products.deleteProduct(createdId, role)
                .then()
                .spec(spec);
    }

    @Test
    public void deleteProductWithInvalidId() {

        int invalidId = 9999999;

        Products.deleteProduct(invalidId, UserRole.ADMIN)
                .then()
                .spec(fail404());
    }
}