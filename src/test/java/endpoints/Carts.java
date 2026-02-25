package endpoints;

import enums.UserRole;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import payloads.request.CartPOJO;
import testBase.BaseClass;

public class Carts {

    public static Response getAllCarts(UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/carts")
                .when()
                .get();
    }

    public static Response getCartById(int id,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/carts/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response createCart(CartPOJO cart,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/carts")
                .body(cart)
                .when()
                .post();
    }

    public static Response updateCart(int id, CartPOJO cart,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/carts/{id}")
                .pathParam("id", id)
                .body(cart)
                .when()
                .put();
    }

    public static Response deleteCart(int id,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/carts/{id}")
                .pathParam("id", id)
                .when()
                .delete();
    }
}