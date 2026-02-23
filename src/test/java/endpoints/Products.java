package endpoints;
import io.restassured.response.Response;
import payloads.request.ProductsPOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Products {

    public static Response getAllProducts(){
        return given()
                .spec(BaseClass.get())
                .basePath("/products")
                .when()
                .get();
    }

    public static Response getProductById(int id){
        return given()
                .spec(BaseClass.get())
                .basePath("/products/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response getProductsByCategory(String categoryName){
        return given()
                .spec(BaseClass.get())
                .basePath("/products/category/{name}")
                .pathParam("name", categoryName)
                .when()
                .get();
    }

    public static Response createProduct(ProductsPOJO product){
        return given()
                .spec(BaseClass.get())
                .basePath("/products")
                .body(product)
                .when()
                .post();
    }

    public static Response updateProduct(int id, ProductsPOJO product){
        return given()
                .spec(BaseClass.get())
                .basePath("/products/{id}")
                .pathParam("id", id)
                .body(product)
                .when()
                .put();
    }

    public static Response deleteProduct(int id){
        return given()
                .spec(BaseClass.get())
                .basePath("/products/{id}")
                .pathParam("id", id)
                .when()
                .delete();
    }
}
