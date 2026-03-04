package tests;

import endpoints.Carts;
import endpoints.Orders;
import endpoints.Products;
import enums.UserRole;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.CartProductPOJO;
import payloads.request.OrderItemPOJO;
import payloads.request.OrderPOJO;
import payloads.response.OrderItemResponsePOJO;
import payloads.response.OrderResponsePOJO;
import testBase.BaseClass;
import utilities.TokenManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

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

        // Step 1: Get user's own orders
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
    public void userShouldNotAccessOtherOrder() {
        // Step 1: Get user's own orders
        int userIdAdmin = TokenManager.getUserId(UserRole.ADMIN);

        List<Integer> orderIds = Orders.getOrdersByUserId(userIdAdmin, UserRole.USER)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("id", Integer.class);

        int randomOrderId = orderIds.get(new Random().nextInt(orderIds.size()-1));

        Orders.getOrderById(randomOrderId, UserRole.USER)
                .then()
                .spec(fail403());

    }

    //user should be able to create a order for himself
    @Test
    public void createOrderByUser(){

        Double totalPrice = 0.0;

        int userId = TokenManager.getUserId(UserRole.USER);
        List<OrderItemPOJO> orderItems = Carts.getCarts(UserRole.USER)
                .then()
                .spec(success200())
                .extract()
                .jsonPath()

                .getList("[0].products", OrderItemPOJO.class);

        OrderPOJO orderPOJO = OrderPOJO.builder()
                .userId(userId)
                .items(orderItems)
                .build();

        OrderResponsePOJO orderResponse =
                Orders.createOrder(orderPOJO , UserRole.USER)
                        .as(OrderResponsePOJO.class);

        Assert.assertEquals(userId , orderResponse.getUserId());
        Assert.assertNotNull(orderResponse.getId());
        Assert.assertEquals(orderResponse.getItems().size(),orderItems.size());

        List<OrderItemResponsePOJO> itemResponse = orderResponse.getItems();

        //Checking of same productIds as order placed
        Map<Integer, Integer> requestMap = orderItems.stream()
                .collect(Collectors.toMap(
                        OrderItemPOJO::getProductId,
                        OrderItemPOJO::getQuantity
                ));

        for (OrderItemResponsePOJO item : itemResponse) {

            Assert.assertTrue(requestMap.containsKey(item.getProductId()),
                    "Unexpected productId in response: " + item.getProductId());

            Assert.assertEquals(
                    item.getQuantity(),
                    requestMap.get(item.getProductId()),
                    "Quantity mismatch for productId " + item.getProductId()
            );

            totalPrice = totalPrice + Products.getProductById(item.getProductId(),UserRole.USER).then().extract().jsonPath().getDouble("price");
        }

        Assert.assertEquals(orderResponse.getTotalAmount(),totalPrice,"The total prices does not match");

        Assert.assertEquals(orderResponse.getStatus(),"PENDING","The order status is different");

        Assert.assertEquals(orderResponse.getOrderDate(),LocalDate.now().toString(),"The order date does not match");

    }

    //User should not be able to create a order for others
    @Test
    public void createOrderByUserForAnotherUser(){

        int userId = TokenManager.getUserId(UserRole.USER);
        List<OrderItemPOJO> orderItems = Carts.getCarts(UserRole.USER)
                .then()
                .spec(success200())
                .extract()
                .jsonPath()

                .getList("[0].products", OrderItemPOJO.class);

        OrderPOJO orderPOJO = OrderPOJO.builder()
                .userId(userId+10)
                .items(orderItems)
                .build();

        Orders.createOrder(orderPOJO , UserRole.USER)
                .then()
                .spec(fail403());

    }

    //admin can create a order for anyone
    @Test
    public void createOrderByAdmin(){

        Double totalPrice = 0.0;

        int userId = TokenManager.getUserId(UserRole.USER);
        List<OrderItemPOJO> orderItems = Carts.getCarts(UserRole.USER)
                .then()
                .spec(success200())
                .extract()
                .jsonPath()

                .getList("[0].products", OrderItemPOJO.class);

        OrderPOJO orderPOJO = OrderPOJO.builder()
                .userId(userId)
                .items(orderItems)
                .build();

        OrderResponsePOJO orderResponse =
                Orders.createOrder(orderPOJO , UserRole.ADMIN)
                        .as(OrderResponsePOJO.class);

        Assert.assertEquals(userId , orderResponse.getUserId());
        Assert.assertNotNull(orderResponse.getId());
        Assert.assertEquals(orderResponse.getItems().size(),orderItems.size());

        List<OrderItemResponsePOJO> itemResponse = orderResponse.getItems();

        //Checking of same productIds as order placed
        Map<Integer, Integer> requestMap = orderItems.stream()
                .collect(Collectors.toMap(
                        OrderItemPOJO::getProductId,
                        OrderItemPOJO::getQuantity
                ));

        for (OrderItemResponsePOJO item : itemResponse) {

            Assert.assertTrue(requestMap.containsKey(item.getProductId()),
                    "Unexpected productId in response: " + item.getProductId());

            Assert.assertEquals(
                    item.getQuantity(),
                    requestMap.get(item.getProductId()),
                    "Quantity mismatch for productId " + item.getProductId()
            );

            totalPrice = totalPrice + Products.getProductById(item.getProductId(),UserRole.USER).then().extract().jsonPath().getDouble("price");
        }

        Assert.assertEquals(orderResponse.getTotalAmount(),totalPrice,"The total prices does not match");

        Assert.assertEquals(orderResponse.getStatus(),"PENDING","The order status is different");

        Assert.assertEquals(orderResponse.getOrderDate(),LocalDate.now().toString(),"The order date does not match");

    }








}