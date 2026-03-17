package tests;

import dataproviders.OrdersDataProvider;
import endpoints.Carts;
import endpoints.Orders;
import endpoints.Products;
import enums.OrderStatus;
import enums.UserRole;
import helpers.OrderHelper;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
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

    //User should not be able to fetch someone else's Orders
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

        Orders.getOrderById(invalidOrderId,UserRole.ADMIN)
                .then()
                .spec(fail404());


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

        List<OrderItemPOJO> orderItems = Carts.getCarts(UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("[0].products", OrderItemPOJO.class);

        int userId = TokenManager.getUserId(UserRole.USER);
        OrderPOJO order = OrderHelper.buildOrder(userId , orderItems);

        System.out.println(order);


        OrderResponsePOJO orderResponse = Orders.createOrder(order,UserRole.USER).then().statusCode(201).extract().as(OrderResponsePOJO.class);

        Assert.assertEquals(orderResponse.getUserId(),userId,"User Id should match");
        Assert.assertEquals(orderResponse.getItems().size(),orderItems.size(),"Order of same number of products should be placed as was present in cart");
        Assert.assertNotNull(orderResponse.getId(),"Order Id should not be null");

        OrderHelper.validateOrderedItems(orderItems,orderResponse.getItems());

        double total = OrderHelper.calculateTotal(orderResponse.getItems());
        Assert.assertEquals(total , orderResponse.getTotalPrice(),"Total price should be same");


    }

    //User should not be able to create a order for others
    @Test
    public void createOrderByUserForAnotherUser(){

        List<OrderItemPOJO> orderItems = Carts.getCarts(UserRole.USER).then().extract().jsonPath().getList("");

        int userId = TokenManager.getUserId(UserRole.USER);
        OrderPOJO order = OrderHelper.buildOrder(userId-1 , orderItems);

        Orders.createOrder(order,UserRole.USER).then().spec(fail403());



    }

    //admin can create a order for anyone
    @Test
    public void createOrderByAdmin(){

        List<OrderItemPOJO> orderItems = Carts.getCarts(UserRole.USER).then().extract().jsonPath().getList("");

        int userId = TokenManager.getUserId(UserRole.USER);
        OrderPOJO order = OrderHelper.buildOrder(userId , orderItems);

        OrderResponsePOJO orderResponse = Orders.createOrder(order,UserRole.ADMIN).then().extract().as(OrderResponsePOJO.class);

        Assert.assertEquals(orderResponse.getUserId(),userId,"User Id should match");
        Assert.assertEquals(orderResponse.getItems().size(),orderItems.size(),"Order of same number of products should be placed as was present in cart");
        Assert.assertNotNull(orderResponse.getId(),"Order Id should not be null");

        OrderHelper.validateOrderedItems(orderItems,orderResponse.getItems());

        double total = OrderHelper.calculateTotal(orderResponse.getItems());
        Assert.assertEquals(total , orderResponse.getTotalPrice(),"Total price should be same");
    }

    @Test
    public void createOrderByUserWithoutUserId(){

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

        OrderHelper.validateOrderedItems(orderItems,orderResponse.getItems());

        double total = OrderHelper.calculateTotal(orderResponse.getItems());
        Assert.assertEquals(total , orderResponse.getTotalPrice(),"Total price should be same");

        Assert.assertEquals(orderResponse.getTotalPrice(),total,"The total prices does not match");

        Assert.assertEquals(orderResponse.getStatus(),"PENDING","The order status is different");

        Assert.assertEquals(orderResponse.getOrderDate(),LocalDate.now().toString(),"The order date does not match");

        Orders.deleteOrder(orderResponse.getId(),UserRole.ADMIN);
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


    @Test(dataProvider = "invalidOrderPayloads", dataProviderClass = OrdersDataProvider.class)
    public void createOrderWithInvalidPayload(String scenario, OrderPOJO order, ResponseSpecification resp){

        System.out.println("Running Scenario: " + scenario);

        Orders.createOrder(order, UserRole.USER)
                .then()
                .spec(resp);
    }

    //user can not update order
    @Test
    public void userUpdatesOrder() {

        List<Integer> id = Orders.getOrders(UserRole.USER)
                .then().spec(success200())
                .extract().jsonPath().getList("id", Integer.class);

        int randomOrderId = id.get(new Random().nextInt(id.size()));

        String body = """
        {
            "userId": %d,
            "items": [
                {
                    "productId": 101,
                    "quantity": 2
                }
            ]
        }
        """.formatted(TokenManager.getUserId(UserRole.USER));

        Orders.updateOrderWithString(randomOrderId, body, UserRole.USER)
                .then()
                .spec(fail403());
    }

    @Test
    public void adminUpdatesOrder(){
        List<Integer> id = Orders.getOrders(UserRole.USER)
                .then().spec(success200())
                .extract().jsonPath().getList("id",Integer.class);

        int randomOrderId = id.get(new Random().nextInt(id.size()-1));

        String body = """
        {
            "userId": %d,
            "items": [
                {
                    "productId": 101,
                    "quantity": 2
                }
            ]
        }
        """.formatted(TokenManager.getUserId(UserRole.ADMIN));

        Orders.updateOrderWithString(randomOrderId,body,UserRole.ADMIN)
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
        Orders.updateOrderWithToken(5001,orderStatusUpdatePOJO,expiredToken)
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

    //admin update invalid status
    @Test
    public void invalidStatusUpdate(){

        List<Integer> id = Orders.getOrders(UserRole.USER)
                .then().spec(success200())
                .extract().jsonPath().getList("id",Integer.class);

        int randomOrderId = id.get(id.size()-1);

        String body = """
                {
                "status":"abcd"
                }
                
                """;
        Orders.updateOrderWithString(randomOrderId,body,UserRole.ADMIN)
                .then().spec(fail400());
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

        int randomOrderId = myOrder.get(myOrder.size()-1);

        Orders.deleteOrder(randomOrderId,UserRole.ADMIN)
                .then().spec(success200());

    }

    @Test
    public void deleteInvalidOrder(){

        Orders.deleteOrder(Integer.MAX_VALUE,UserRole.ADMIN)
                .then()
                .spec(fail404());
    }




}