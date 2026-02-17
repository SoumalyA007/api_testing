package endpoints;
import io.restassured.response.Response;
import payloads.PostPOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Products {



    public static Response get_all_products(){

        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/products")
                .when()
                .get();

        return resp;

    }

    public static Response add_new_product(PostPOJO post_body){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/products")
                .body(post_body)
                .when()
                .post();
        return resp;

    }

    public static Response get_single_product(int id){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/products/{id}")
                .pathParam("id",id)
                .when()
                .get();
        return resp;

    }

    public static Response update_product(int id,PostPOJO update_body){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/products/{id}")
                .pathParam("id",id)
                .body(update_body)
                .when()
                .put();
        return resp;

    }

    public static Response delete_product(int id){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/products/{id}")
                .pathParam("id",id)
                .when()
                .delete();
        return resp;

    }







}
