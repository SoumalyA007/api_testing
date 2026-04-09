package tests;
import dataproviders.OrdersDataProvider;
import endpoints.Orders;
import enums.OrderStatus;
import enums.UserRole;
import helpers.OrderHelper;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.OrderItemPOJO;
import payloads.request.OrderPOJO;
import payloads.request.OrderStatusUpdatePOJO;
import payloads.response.OrderResponsePOJO;
import testBase.BaseClass;
import testData.OrderTestDataFactory;
import utilities.TokenManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import static org.hamcrest.Matchers.*;
public class OrdersTest extends BaseClass {
    private final Random random = new Random();
    // ================= GET ORDERS =================
    @Test(groups = {"smoke", "orders"}, priority = 1)
    public void userShouldGetOnlyOwnOrders() {
        int currentUserId = TokenManager.getUserId(UserRole.USER);
        Orders.getOrders(UserRole.USER)
                .then()
                .spec(success200())
                .body("userId", everyItem(equalTo(currentUserId)));
    }

    @Test(groups = {"smoke", "orders"}, priority = 2)
    public void adminShouldGetAllOrders() {
        Response response = Orders.getOrders(UserRole.ADMIN);
        response.then().spec(success200());
        List<Integer> userIds = response.jsonPath().getList("userId");
        Assert.assertTrue(userIds.stream().distinct().count() > 1,
                "Admin should see multiple users' orders");
    }

    @Test(groups = {"regression", "orders"}, priority = 3)
    public void userShouldGetOrdersByUserId() {
        int userId = TokenManager.getUserId(UserRole.USER);
        Orders.getOrdersByUserId(userId, UserRole.USER)
                .then()
                .spec(success200())
                .body("userId", everyItem(equalTo(userId)));
    }

    @Test(groups = {"security", "orders"}, priority = 4)
    public void userShouldNotAccessOtherUsersOrders(){    
        int otherUserId = TokenManager.getUserId(UserRole.ADMIN);
        Orders.getOrdersByUserId(otherUserId, UserRole.USER)
                .then()
                .spec(fail403());
    }

    @Test(groups = {"security", "orders"}, priority = 5)
    public void adminCanAccessAnyUsersOrders() {
        int userId = TokenManager.getUserId(UserRole.USER);
        Orders.getOrdersByUserId(userId, UserRole.ADMIN)
                .then()
                .spec(success200())
                .body("userId", everyItem(equalTo(userId)));
    }

    @Test(groups = {"security", "orders"}, priority = 6)
    public void shouldReturn401ForExpiredToken() {
        String expiredToken = TokenManager.generateExpiredToken(UserRole.USER);
        Orders.getOrders(expiredToken)
                .then()
                .spec(fail401());
    }

    @Test(groups = {"negative", "orders"}, priority = 7)
    public void shouldReturn404ForInvalidOrderId() {
        Orders.getOrderById(Integer.MAX_VALUE, UserRole.ADMIN)
                .then()
                .spec(fail404());
    }

    // ================= GET ORDER BY ID =================
    @Test(groups = {"regression", "orders"}, priority = 8)
    public void userShouldAccessOwnOrder() {
        int userId = TokenManager.getUserId(UserRole.USER);
        List<Integer> orderIds = Orders.getOrdersByUserId(userId, UserRole.USER)
                .then()
                .spec(success200())
                .extract()
                .jsonPath()
                .getList("id", Integer.class);
        int orderId = orderIds.get(random.nextInt(orderIds.size()));
        Orders.getOrderById(orderId, UserRole.USER)
                .then()
                .spec(success200())
                .body("id", equalTo(orderId));
    }

    @Test(groups = {"security", "orders"}, priority = 9)
    public void userShouldNotAccessOthersOrderById() {
        int adminUserId = TokenManager.getUserId(UserRole.ADMIN);
        List<Integer> orderIds = Orders.getOrdersByUserId(adminUserId, UserRole.ADMIN)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class);
        int orderId = orderIds.get(random.nextInt(orderIds.size()));
        Orders.getOrderById(orderId, UserRole.USER)
                .then()
                .spec(fail403());
    }

    // ================= CREATE ORDER =================
    @Test(groups = {"crud", "integration", "orders", "smoke"}, priority = 10)
    public void userShouldCreateOrder() {
        List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
        int userId = TokenManager.getUserId(UserRole.USER);
        OrderPOJO order = OrderTestDataFactory.validOrder(userId, items);
        OrderResponsePOJO response = Orders.createOrder(order, UserRole.USER)
                .then()
                .statusCode(201)
                .extract()
                .as(OrderResponsePOJO.class);
        int orderId =  response.getId();
        try {
            Assert.assertEquals(response.getUserId(), userId);
            Assert.assertEquals(response.getItems().size(), items.size());
            OrderHelper.validateOrderedItems(items, response.getItems());
            double expectedTotal = OrderHelper.calculateTotal(response.getItems());
            Assert.assertEquals(response.getTotalPrice(), expectedTotal, 0.01);
        } finally {
            OrderHelper.deleteOrderIfExists(orderId);
        }
    }

    @Test(groups = {"security", "orders"},priority = 11)
    public void userShouldNotCreateOrderForAnotherUser() {
        int orderId = 0;
        int statusCode = 0;
        try{
            List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
            int userId = TokenManager.getUserId(UserRole.USER);
            OrderPOJO order = OrderTestDataFactory.validOrder(userId - 1, items);
            Response response = Orders.createOrder(order, UserRole.USER);
            response.then().spec(fail403());
            statusCode = response.statusCode();
            orderId =  response.then().extract().jsonPath().getInt("id");
        }finally {
            if(statusCode != 403){
                OrderHelper.deleteOrderIfExists(orderId);
            }
        }
    }

    @Test(groups = {"crud", "orders"}, priority = 12, alwaysRun = true)
    public void adminShouldCreateOrder() {
        int orderId = 0;
        try{
                List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
                int userId = TokenManager.getUserId(UserRole.USER);
        
                OrderPOJO order = OrderTestDataFactory.validOrder(userId, items);
                Response response = Orders.createOrder(order, UserRole.ADMIN);
                // 🔥 Extract FIRST (safe)
                orderId = response.jsonPath().get("id");
                // 🔥 Then assert
                response.then().statusCode(201);
        }finally{
            OrderHelper.deleteOrderIfExists(orderId);
        }
        
    }

    @Test(groups = {"negative", "orders"}, priority = 13, alwaysRun = true)
    public void createOrderWithoutUserId() {
        int orderId = 0;
        try {
            List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
            OrderPOJO order = OrderTestDataFactory.orderWithoutUserId(items);
            Response response = Orders.createOrder(order, UserRole.ADMIN);
            OrderResponsePOJO orderResponsePOJO = response.then().extract().as(OrderResponsePOJO.class);
            // 🔥 Extract FIRST (safe)
            orderId = response.jsonPath().get("id");
            // 🔥 Then assert
            response.then().statusCode(201);
            Assert.assertNotNull(orderResponsePOJO.getId());
            Assert.assertEquals(orderResponsePOJO.getStatus(), "PENDING");
            Assert.assertEquals(orderResponsePOJO.getOrderDate(), LocalDate.now().toString());
        } finally {
            OrderHelper.deleteOrderIfExists(orderId);
        }
    }

    @Test(groups = {"security", "orders"}, priority = 14)
    public void createOrderWithoutLogin() {
        int orderId = 0 ;
        try{
            List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
            int userId = TokenManager.getUserId(UserRole.USER);
            OrderPOJO order = OrderTestDataFactory.validOrder(userId, items);
            Response response = Orders.createOrder(order, null);
            orderId = response.then().extract().jsonPath().getInt("id");
            response.then().spec(fail401());
        }finally {
            OrderHelper.deleteOrderIfExists(orderId);
    }
}

    // ================= INVALID PAYLOAD =================
    @Test(
        dataProvider = "invalidOrderPayloads",
        dataProviderClass = OrdersDataProvider.class,
        groups = {"negative", "orders"},
        priority = 15
    )
    public void createOrderWithInvalidPayload(String scenario, OrderPOJO order, ResponseSpecification spec) {
        int orderId = 0;
        try{
            System.out.println("Scenario: " + scenario);
            Response response =Orders.createOrder(order, UserRole.USER);
            response.then().spec(spec);
            orderId = response.then().extract().jsonPath().getInt("id");
        }finally{
            OrderHelper.deleteOrderIfExists(orderId);
        }
    }

    // ================= UPDATE =================
    @Test(groups = {"security", "orders"}, priority = 16)
    public void userShouldNotUpdateOrder() {
        int orderId = 0;
        try{
            List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
            int userId = TokenManager.getUserId(UserRole.USER);
            OrderPOJO order = OrderTestDataFactory.validOrder(userId, items);
            orderId = Orders.createOrder(order, UserRole.USER).then()
                    .extract().jsonPath().getInt("id");
            OrderStatusUpdatePOJO orderStatusUpdatePOJO = OrderStatusUpdatePOJO.builder().status(OrderStatus.SHIPPED).build();
            Orders.updateOrder(orderId, orderStatusUpdatePOJO,UserRole.USER).then()
                    .spec(fail403());
        }finally {
            OrderHelper.deleteOrderIfExists(orderId);
        }
    }

    @Test(groups = {"crud", "orders"}, priority = 17)
    public void adminShouldUpdateOrderStatus() {
        int orderId = 0;
        try{
            List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
            int userId = TokenManager.getUserId(UserRole.USER);
            OrderPOJO order = OrderTestDataFactory.validOrder(userId, items);
            orderId = Orders.createOrder(order, UserRole.USER).then()
                    .extract().jsonPath().getInt("id");
            OrderStatusUpdatePOJO payload = OrderStatusUpdatePOJO.builder()
                    .status(OrderStatus.SHIPPED)
                    .build();
            Orders.updateOrder(orderId, payload, UserRole.ADMIN)
                    .then()
                    .spec(success200())
                    .body("status", equalTo("SHIPPED"));
        }finally {
            OrderHelper.deleteOrderIfExists(orderId);
        }
    }

    @Test(groups = {"security", "orders"}, priority = 18)
    public void adminUpdateWithExpiredToken() {
        int orderId = 0;
        try{
            List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
            int userId = TokenManager.getUserId(UserRole.USER);
            OrderPOJO order = OrderTestDataFactory.validOrder(userId, items);
            orderId = Orders.createOrder(order, UserRole.USER).then()
                    .extract().jsonPath().getInt("id");
            String expiredToken = TokenManager.generateExpiredToken(UserRole.ADMIN);
            OrderStatusUpdatePOJO payload = OrderStatusUpdatePOJO.builder()
                    .status(OrderStatus.SHIPPED)
                    .build();
            Orders.updateOrderWithToken(orderId, payload, expiredToken)
                    .then()
                    .spec(fail401());
        }finally {
            OrderHelper.deleteOrderIfExists(orderId);
        }
    }

    @Test(groups = {"negative", "orders"}, priority = 19)
    public void invalidStatusUpdateShouldFail() {
        int orderId = 0;
        try{
            List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
            int userId = TokenManager.getUserId(UserRole.USER);
            OrderPOJO order = OrderTestDataFactory.validOrder(userId, items);
            orderId = Orders.createOrder(order, UserRole.USER).then()
                    .extract().jsonPath().getInt("id");
            String body = """
        {
            "status": "INVALID_STATUS"
        }
        """;
            Orders.updateOrderWithString(orderId, body, UserRole.ADMIN)
                    .then()
                    .spec(fail400());
        }finally {
            OrderHelper.deleteOrderIfExists(orderId);        }
    }

    // ================= DELETE =================
    @Test(groups = {"crud", "orders"}, priority = 20)
    public void userShouldDeleteOwnOrder() {
        int userId = TokenManager.getUserId(UserRole.USER);
        List<Integer> orderIds = Orders.getOrdersByUserId(userId, UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class);
        int orderId = orderIds.get(orderIds.size() - 1);
        Orders.deleteOrder(orderId, UserRole.USER)
                .then()
                .spec(success200());
    }
    
    @Test(groups = {"crud", "orders"}, priority = 21)
    public void adminShouldDeleteOrder() {
        int userId = TokenManager.getUserId(UserRole.USER);
        List<Integer> orderIds = Orders.getOrdersByUserId(userId, UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class);
        int orderId = orderIds.get(orderIds.size() - 1);
        Orders.deleteOrder(orderId, UserRole.ADMIN)
                .then()
                .spec(success200());
    }

    @Test(groups = {"negative", "orders"}, priority = 22)
    public void deleteInvalidOrderShouldReturn404() {
        Orders.deleteOrder(Integer.MAX_VALUE, UserRole.ADMIN)
                .then()
                .spec(fail404());
    }
}