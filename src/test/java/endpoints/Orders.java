package endpoints;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import payloads.request.OrderPOJO;
import testBase.BaseClass;


public class Orders {

    public static Response getAllOrders(){
        return given()
                .spec(BaseClass.get())
                .basePath("/orders")
                .when()
                .get();
    }

    public static Response getOrderById(int id){
        return given()
                .spec(BaseClass.get())
                .basePath("/orders/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response getOrdersByUserId(int userId){
        return given()
                .spec(BaseClass.get())
                .basePath("/my-orders")
                .queryParam("userId", userId)
                .when()
                .get();
    }

    public static Response createOrder(OrderPOJO order){
        return given()
                .spec(BaseClass.get())
                .basePath("/orders")
                .body(order)
                .when()
                .post();
    }

    public static Response updateOrder(int id, OrderPOJO order){
        return given()
                .spec(BaseClass.get())
                .basePath("/orders/{id}")
                .pathParam("id", id)
                .body(order)
                .when()
                .put();
    }

    public static Response deleteOrder(int id){
        return given()
                .spec(BaseClass.get())
                .basePath("/orders/{id}")
                .pathParam("id", id)
                .when()
                .delete();
    }
}