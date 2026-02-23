package endpoints;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import testBase.BaseClass;

public class Shipping {

    public static Response getAllShipping(){
        return given()
                .spec(BaseClass.get())
                .basePath("/shipping")
                .when()
                .get();
    }

    public static Response getShippingById(int id){
        return given()
                .spec(BaseClass.get())
                .basePath("/shipping/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response trackOrder(int orderId){
        return given()
                .spec(BaseClass.get())
                .basePath("/track/{id}")
                .pathParam("id", orderId)
                .when()
                .get();
    }
}