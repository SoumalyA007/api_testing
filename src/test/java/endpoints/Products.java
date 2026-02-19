package endpoints;
import io.restassured.response.Response;
import payloads.ProductsPOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Products {



    public static Response getProducts(){

        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/products")
                .when()
                .get();

        return resp;

    }

    public static Response createProduct(ProductsPOJO post_body){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/products")
                .body(post_body)
                .when()
                .post();
        return resp;

    }

    public static Response getProduct(int id){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/products/{id}")
                .pathParam("id",id)
                .when()
                .get();
        return resp;

    }

    public static Response updateProduct(int id, ProductsPOJO update_body){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/products/{id}")
                .pathParam("id",id)
                .body(update_body)
                .when()
                .put();
        return resp;

    }

    public static Response deleteProduct(int id){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/products/{id}")
                .pathParam("id",id)
                .when()
                .delete();
        return resp;

    }







}
