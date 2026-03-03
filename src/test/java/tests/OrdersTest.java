package tests;

import endpoints.Carts;
import endpoints.Orders;
import enums.UserRole;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.OrderItemPOJO;
import testBase.BaseClass;
import utilities.TokenManager;

import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;

public class OrdersTest extends BaseClass {

    //User should fetch only his orders
    @Test
    public void getOrderUser(){

        int currentUserId = TokenManager.getUserId(UserRole.USER);
        Orders.getOrders(UserRole.USER).then()
                .spec(success200())
                .body("userId",everyItem(equalTo(currentUserId)));

    }

    //admin should be able to fetch all orders of all users
    @Test
    public void getOrderAdmin(){

        Response response = Orders.getOrders(UserRole.ADMIN);
        response.then().spec(success200());
        List<Integer> userIds = response.jsonPath().getList("userId");
        Assert.assertTrue(userIds.stream().distinct().count() > 1,
                "Admin should see orders from multiple users");

    }

    //User should be able to fetch his personal Orders
    @Test
    public void getOrderById(){

        int currentUserId = TokenManager.getUserId(UserRole.USER);

        Orders.getOrdersByUserId(currentUserId,UserRole.USER)
                .then()
                .spec(success200())
                .body("userId",everyItem(equalTo(currentUserId)));
    }

    //User should be able to fetch his personal Orders
    @Test
    public void getOrderByRandomId(){

        int currentUserId = TokenManager.getUserId(UserRole.ADMIN);

        Orders.getOrdersByUserId(currentUserId,UserRole.USER)
                .then()
                .spec(fail403());
    }

    //Admin can get anyone's order
    @Test
    public void getOrderByRandomIdByAdmin(){

        int userId = TokenManager.getUserId(UserRole.USER);

        Orders.getOrdersByUserId(userId,UserRole.ADMIN)
                .then().spec(success200())
                .body("userId",everyItem(equalTo(userId)));
    }

    //User access only his order
    @Test
    public void userShouldAccessOwnOrder() {

        // Step 1: Get user’s own orders
        List<Integer> orderIds = Orders.getOrdersByUserId(1, UserRole.USER)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("id", Integer.class);

        int ownOrderId = orderIds.get(0);

        // Step 2: Fetch that order
        Orders.getOrderById(ownOrderId, UserRole.USER)
                .then()
                .statusCode(200)
                .body("id", equalTo(ownOrderId));
    }

    //User can not access other orderIds
    @Test
    public void userShouldNotAccessOtherOrder(){
        // Step 1: Get user’s own orders
        List<Integer> orderIds = Orders.getOrdersByUserId(1, UserRole.USER)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("id", Integer.class);
    }

    //user should be able to create a order for himself
    @Test
    public void createOrderByUser(){
        int userId = TokenManager.getUserId(UserRole.USER);
        List<Integer> cartIds = Carts.getCarts(UserRole.USER).then().spec(success200())
                .extract().jsonPath().getList("id",Integer.class);

        int cartId = cartIds.get(0);





    }



}
