package endpoints;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import testBase.BaseClass;

public class Categories {

    public static Response getCategories(){
        return given()
                .spec(BaseClass.get())
                .basePath("/products/categories")
                .when()
                .get();
    }

    public static Response getProductsByCategory(String name){
        return given()
                .spec(BaseClass.get())
                .basePath("/products/category/{name}")
                .pathParam("name", name)
                .when()
                .get();
    }
}
