package endpoints;
import enums.UserRole;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import testBase.BaseClass;

public class Categories {

    public static Response getCategories(UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/products/categories")
                .when()
                .get();
    }

    public static Response getProductsByCategory(String name,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/products/category/{name}")
                .pathParam("name", name)
                .when()
                .get();
    }
}
