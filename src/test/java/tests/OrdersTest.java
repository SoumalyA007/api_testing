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

    @Test
    public void userShouldGetOnlyOwnOrders() {
        int currentUserId = TokenManager.getUserId(UserRole.USER);

        Orders.getOrders(UserRole.USER)
                .then()
                .spec(success200())
                .body("userId", everyItem(equalTo(currentUserId)));
    }

    @Test
    public void adminShouldGetAllOrders() {
        Response response = Orders.getOrders(UserRole.ADMIN);

        response.then().spec(success200());

        List<Integer> userIds = response.jsonPath().getList("userId");
        Assert.assertTrue(userIds.stream().distinct().count() > 1,
                "Admin should see multiple users' orders");
    }

    @Test
    public void userShouldGetOrdersByUserId() {
        int userId = TokenManager.getUserId(UserRole.USER);

        Orders.getOrdersByUserId(userId, UserRole.USER)
                .then()
                .spec(success200())
                .body("userId", everyItem(equalTo(userId)));
    }

    @Test
    public void userShouldNotAccessOtherUsersOrders() {
        int otherUserId = TokenManager.getUserId(UserRole.ADMIN);

        Orders.getOrdersByUserId(otherUserId, UserRole.USER)
                .then()
                .spec(fail403());
    }

    @Test
    public void adminCanAccessAnyUsersOrders() {
        int userId = TokenManager.getUserId(UserRole.USER);

        Orders.getOrdersByUserId(userId, UserRole.ADMIN)
                .then()
                .spec(success200())
                .body("userId", everyItem(equalTo(userId)));
    }

    @Test
    public void shouldReturn401ForExpiredToken() {
        String expiredToken = TokenManager.generateExpiredToken(UserRole.USER);

        Orders.getOrders(expiredToken)
                .then()
                .spec(fail401());
    }

    @Test
    public void shouldReturn404ForInvalidOrderId() {
        Orders.getOrderById(Integer.MAX_VALUE, UserRole.ADMIN)
                .then()
                .spec(fail404());
    }

    // ================= GET ORDER BY ID =================

    @Test
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

    @Test
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

    @Test
    public void userShouldCreateOrder() {
        List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
        int userId = TokenManager.getUserId(UserRole.USER);

        OrderPOJO order = OrderTestDataFactory.validOrder(userId, items);

        OrderResponsePOJO response = Orders.createOrder(order, UserRole.USER)
                .then()
                .statusCode(201)
                .extract()
                .as(OrderResponsePOJO.class);

        try {
            Assert.assertEquals(response.getUserId(), userId);
            Assert.assertEquals(response.getItems().size(), items.size());

            OrderHelper.validateOrderedItems(items, response.getItems());

            double expectedTotal = OrderHelper.calculateTotal(response.getItems());
            Assert.assertEquals(response.getTotalPrice(), expectedTotal, 0.01);

        } finally {
            OrderHelper.deleteOrderIfExists(response.getId());
        }
    }

    @Test
    public void userShouldNotCreateOrderForAnotherUser() {
        List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
        int userId = TokenManager.getUserId(UserRole.USER);

        OrderPOJO order = OrderTestDataFactory.validOrder(userId - 1, items);

        Orders.createOrder(order, UserRole.USER)
                .then()
                .spec(fail403());
    }

    @Test
    public void adminShouldCreateOrder() {
        List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
        int userId = TokenManager.getUserId(UserRole.USER);

        OrderPOJO order = OrderTestDataFactory.validOrder(userId, items);

        OrderResponsePOJO response = Orders.createOrder(order, UserRole.ADMIN)
                .then()
                .statusCode(201)
                .extract()
                .as(OrderResponsePOJO.class);

        OrderHelper.deleteOrderIfExists(response.getId());
    }

    @Test
    public void createOrderWithoutUserId() {
        List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);

        OrderPOJO order = OrderTestDataFactory.orderWithoutUserId(items);

        OrderResponsePOJO response = Orders.createOrder(order, UserRole.USER)
                .then()
                .statusCode(201)
                .extract()
                .as(OrderResponsePOJO.class);

        try {
            Assert.assertNotNull(response.getId());
            Assert.assertEquals(response.getStatus(), "PENDING");
            Assert.assertEquals(response.getOrderDate(), LocalDate.now().toString());
        } finally {
            OrderHelper.deleteOrderIfExists(response.getId());
        }
    }

    @Test
    public void createOrderWithoutLogin() {
        List<OrderItemPOJO> items = OrderHelper.getCartProducts(UserRole.USER);
        int userId = TokenManager.getUserId(UserRole.USER);

        OrderPOJO order = OrderTestDataFactory.validOrder(userId, items);

        Orders.createOrder(order, null)
                .then()
                .spec(fail401());
    }

    // ================= INVALID PAYLOAD =================

    @Test(dataProvider = "invalidOrderPayloads", dataProviderClass = OrdersDataProvider.class)
    public void createOrderWithInvalidPayload(
            String scenario,
            OrderPOJO order,
            ResponseSpecification spec) {

        System.out.println("Scenario: " + scenario);

        Orders.createOrder(order, UserRole.USER)
                .then()
                .spec(spec);
    }

    // ================= UPDATE =================

    @Test
    public void userShouldNotUpdateOrder() {
        int userId = TokenManager.getUserId(UserRole.USER);

        List<Integer> orderIds = Orders.getOrdersByUserId(userId, UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class);

        int orderId = orderIds.get(random.nextInt(orderIds.size()));

        String body = """
        {
            "status": "PAID"
        }
        """;

        Orders.updateOrderWithString(orderId, body, UserRole.USER)
                .then()
                .spec(fail403());
    }

    @Test
    public void adminShouldUpdateOrderStatus() {
        OrderStatusUpdatePOJO payload = OrderStatusUpdatePOJO.builder()
                .status(OrderStatus.PAID)
                .build();

        Orders.updateOrder(5001, payload, UserRole.ADMIN)
                .then()
                .spec(success200())
                .body("status", equalTo("PAID"));
    }

    @Test
    public void adminUpdateWithExpiredToken() {
        String expiredToken = TokenManager.generateExpiredToken(UserRole.ADMIN);

        OrderStatusUpdatePOJO payload = OrderStatusUpdatePOJO.builder()
                .status(OrderStatus.PAID)
                .build();

        Orders.updateOrderWithToken(5001, payload, expiredToken)
                .then()
                .spec(fail401());
    }

    @Test
    public void invalidStatusUpdateShouldFail() {
        int userId = TokenManager.getUserId(UserRole.USER);

        List<Integer> orderIds = Orders.getOrdersByUserId(userId, UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class);

        int orderId = orderIds.get(orderIds.size() - 1);

        String body = """
        {
            "status": "INVALID_STATUS"
        }
        """;

        Orders.updateOrderWithString(orderId, body, UserRole.ADMIN)
                .then()
                .spec(fail400());
    }

    // ================= DELETE =================

    @Test
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

    @Test
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

    @Test
    public void deleteInvalidOrderShouldReturn404() {
        Orders.deleteOrder(Integer.MAX_VALUE, UserRole.ADMIN)
                .then()
                .spec(fail404());
    }
}