package endpoints;
import enums.UserRole;
import io.restassured.response.Response;
import payloads.request.ProductsPOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Products {

    public static Response getAllProducts(UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/products")
                .when()
                .get();
    }

    public static Response getProductById(int id,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/products/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response getProductsByCategory(String categoryName , UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/products/category/{name}")
                .pathParam("name", categoryName)
                .when()
                .get();
    }

    public static Response createProduct(ProductsPOJO product,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/products")
                .body(product)
                .when()
                .post();
    }

    public static Response updateProduct(int id, ProductsPOJO product,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/products/{id}")
                .pathParam("id", id)
                .body(product)
                .when()
                .put();
    }

    public static Response deleteProduct(int id,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/products/{id}")
                .pathParam("id", id)
                .when()
                .delete();
    }
}
