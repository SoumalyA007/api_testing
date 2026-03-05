package tests;

import endpoints.Carts;
import endpoints.Orders;
import endpoints.Products;
import enums.OrderStatus;
import enums.UserRole;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.config.Order;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.mozilla.javascript.Token;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.CartProductPOJO;
import payloads.request.OrderItemPOJO;
import payloads.request.OrderPOJO;
import payloads.request.OrderStatusUpdatePOJO;
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

@Slf4j
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

            double price = Products.getProductById(item.getProductId(),UserRole.USER).then().extract().jsonPath().getDouble("price");
            totalPrice += price* item.getQuantity();
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

            double price = Products.getProductById(item.getProductId(),UserRole.USER).then().extract().jsonPath().getDouble("price");
            totalPrice += price* item.getQuantity();
        }

        Assert.assertEquals(orderResponse.getTotalAmount(),totalPrice,"The total prices does not match");

        Assert.assertEquals(orderResponse.getStatus(),"PENDING","The order status is different");

        Assert.assertEquals(orderResponse.getOrderDate(),LocalDate.now().toString(),"The order date does not match");

    }

    @Test
    public void createOrderByUserWithoutUserId(){
        Double totalPrice = 0.0;


        List<OrderItemPOJO> orderItems = Carts.getCarts(UserRole.USER)
                .then()
                .spec(success200())
                .extract()
                .jsonPath()

                .getList("[0].products", OrderItemPOJO.class);

        OrderPOJO orderPOJO = OrderPOJO.builder()
                .items(orderItems)
                .build();

        OrderResponsePOJO orderResponse =
                Orders.createOrder(orderPOJO , UserRole.USER)
                        .as(OrderResponsePOJO.class);

        Assert.assertEquals(TokenManager.getUserId(UserRole.USER) , orderResponse.getUserId());
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

            double price = Products.getProductById(item.getProductId(),UserRole.USER).then().extract().jsonPath().getDouble("price");
            totalPrice += price* item.getQuantity();
        }

        Assert.assertEquals(orderResponse.getTotalAmount(),totalPrice,"The total prices does not match");

        Assert.assertEquals(orderResponse.getStatus(),"PENDING","The order status is different");

        Assert.assertEquals(orderResponse.getOrderDate(),LocalDate.now().toString(),"The order date does not match");
    }

    @Test
    public void createOrderWithInvalidProductId() {

        OrderPOJO order = OrderPOJO.builder()
                .items(List.of(new OrderItemPOJO(Integer.MAX_VALUE,1)))
                .build();

        Orders.createOrder(order,UserRole.USER)
                .then()
                .statusCode(400);
    }

    @Test
    public void createOrderWithNegativeProductId() {

        OrderPOJO order = OrderPOJO.builder()
                .items(List.of(new OrderItemPOJO(-100,1)))
                .build();

        Orders.createOrder(order,UserRole.USER)
                .then()
                .statusCode(400);
    }

    @Test
    public void createOrderWithEmptyItemList(){

        OrderPOJO order = OrderPOJO.builder()
                .userId(TokenManager.getUserId(UserRole.USER))
                .build();

        Orders.createOrder(order , UserRole.USER)
                .then()
                .spec(fail400());
    }

    @Test
    public void createOrderByWithOutLogin(){

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


        Orders.createOrder(orderPOJO , null)
                .then().spec(fail401());

    }

    //401 Invalid or expired token
    @Test
    public void getOrderForLoggedOutUser(){

        String expiredToken = TokenManager.generateExpiredToken(UserRole.USER);
        Orders.getOrders(expiredToken).then()
                .spec(fail401());

    }

    //404 should be returned when
    @Test
    public void getOrderByInvalidOrderId(){

        int invalidOrderId = Integer.MAX_VALUE;

        Orders.getOrderById(invalidOrderId,UserRole.USER)
                .then()
                .spec(fail404());


    }

    //user can not update order
    @Test
    public void userUpdatesOrder(){
        List<Integer> id = Orders.getOrders(UserRole.USER)
                .then().spec(success200())
                .extract().jsonPath().getList("id",Integer.class);

        int randomOrderId = id.get(new Random().nextInt(id.size()-1));

        OrderItemPOJO orderItemPOJO = OrderItemPOJO.builder()
                        .productId(101)
                                .quantity(2).build();

        OrderPOJO orderPOJO = OrderPOJO.builder()
                        .items(List.of(orderItemPOJO))
                                .userId(TokenManager.getUserId(UserRole.USER))
                                        .build();

        Orders.updateOrder(randomOrderId,orderPOJO,UserRole.USER)
                .then()
                .spec(fail403());
    }

    @Test
    public void adminUpdatesOrder(){
        List<Integer> id = Orders.getOrders(UserRole.USER)
                .then().spec(success200())
                .extract().jsonPath().getList("id",Integer.class);

        int randomOrderId = id.get(new Random().nextInt(id.size()-1));

        OrderItemPOJO orderItemPOJO = OrderItemPOJO.builder()
                .productId(101)
                .quantity(2).build();

        OrderPOJO orderPOJO = OrderPOJO.builder()
                .items(List.of(orderItemPOJO))
                .userId(TokenManager.getUserId(UserRole.ADMIN))
                .build();

        Orders.updateOrder(randomOrderId,orderPOJO,UserRole.ADMIN)
                .then()
                .spec(fail403());
    }

    //Admin can update status of the order
    @Test
    public void adminUpdatesOrderStatus(){
        OrderStatusUpdatePOJO orderStatusUpdatePOJO = OrderStatusUpdatePOJO.builder()
                .status(OrderStatus.PAID)
                .build();

        Orders.updateOrder(5001,orderStatusUpdatePOJO,UserRole.ADMIN)
                .then()
                .spec(success200())
                .body("id" , equalTo(5001))
                .body("status",equalTo("PAID"));
    }

    @Test
    public void adminUpdatesOrderStatusWithExpiredToken(){
        OrderStatusUpdatePOJO orderStatusUpdatePOJO = OrderStatusUpdatePOJO.builder()
                .status(OrderStatus.PAID)
                .build();

        String expiredToken = TokenManager.generateExpiredToken(UserRole.ADMIN);
        Orders.updateOrder(5001,orderStatusUpdatePOJO,expiredToken)
                .then()
                .spec(fail401());
    }

    //User Update OrderStatus
    @Test
    public void userUpdatesOrderStatus(){
        OrderStatusUpdatePOJO orderStatusUpdatePOJO = OrderStatusUpdatePOJO.builder()
                .status(OrderStatus.PAID)
                .build();

        Orders.updateOrder(5001,orderStatusUpdatePOJO,UserRole.USER)
                .then()
                .spec(fail403());
    }


    //delete order by user
    @Test
    public void userDeleteOrder(){

        List<Integer> myOrder = Orders.getOrders(UserRole.USER)
                .then().spec(success200()).extract().jsonPath().getList("id", Integer.class);

        int randomOrderId = myOrder.get(myOrder.size()-1);

        Orders.deleteOrder(randomOrderId,UserRole.USER)
                .then().spec(success200());


    }

    //delete order by admin
    @Test
    public void adminDeleteOrder(){

        List<Integer> myOrder = Orders.getOrders(UserRole.USER)
                .then().spec(success200()).extract().jsonPath().getList("id", Integer.class);

        int randomOrderId = myOrder.get(-1);

        Orders.deleteOrder(randomOrderId,UserRole.ADMIN)
                .then().spec(success200());

    }


}