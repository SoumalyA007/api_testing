package endpoints;
import enums.UserRole;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import payloads.request.OrderPOJO;
import payloads.request.OrderStatusUpdatePOJO;
import testBase.BaseClass;


public class Orders {

    public static Response getOrders(UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/orders")
                .when()
                .get();
    }

    public static Response getOrders(String token){
        return given()
                .spec(BaseClass.getWithToken(token))
                .basePath("/orders")
                .when()
                .get();
    }

    public static Response getOrdersWithOutAuth() {

        return given()
                .basePath("/orders")
                .when()
                .get();
    }

    public static Response getOrderById(int id,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/orders/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response getOrdersByUserId(int userId,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/my-orders")
                .queryParam("userId", userId)
                .when()
                .get();
    }

    public static Response createOrder(OrderPOJO order,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/orders")
                .body(order)
                .when()
                .post();
    }

    public static Response updateOrder(int id, OrderStatusUpdatePOJO order, UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/orders/{id}")
                .pathParam("id", id)
                .body(order)
                .when()
                .put();
    }

    public static Response updateOrderWithString(int id, String order, UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/orders/{id}")
                .pathParam("id", id)
                .body(order)
                .when()
                .put();
    }

    public static Response updateOrderWithToken(int id, OrderStatusUpdatePOJO order, String token){
        return given()
                .spec(BaseClass.getWithToken(token))
                .basePath("/orders/{id}")
                .pathParam("id", id)
                .body(order)
                .when()
                .put();
    }

    public static Response deleteOrder(int id,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/orders/{id}")
                .pathParam("id", id)
                .when()
                .delete();
    }


}