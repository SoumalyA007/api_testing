package endpoints;
import enums.UserRole;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import testBase.BaseClass;

public class Shipping {

    public static Response getAllShipping(UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/shipping")
                .when()
                .get();
    }

    public static Response getShippingById(int id, UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/shipping/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response trackOrder(int orderId , UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/track/{id}")
                .pathParam("id", orderId)
                .when()
                .get();
    }
}