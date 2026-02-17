package endpoints;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import payloads.CartPOJO;
import testBase.BaseClass;

public class Carts {

    public static Response get_all_cart(){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/carts")
                .when()
                .get();

        return resp;
    }

    public static Response add_new_cart(CartPOJO cart){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/carts")
                .body(cart)
                .when()
                .post();

        return resp;
    }

    public static Response get_single_cart(int id){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/carts/{id}")
                .pathParam("id",id)
                .when()
                .get();

        return resp;
    }

    public static Response update_cart(CartPOJO cart,int id){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/carts/{id}")
                .pathParam("id",id)
                .body(cart)
                .when()
                .put();

        return resp;
    }

    public static Response delete_cart(int id){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/carts/{id}")
                .pathParam("id",id)
                .when()
                .delete();

        return resp;
    }









}
