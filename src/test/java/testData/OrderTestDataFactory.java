package testData;

import payloads.request.OrderItemPOJO;
import payloads.request.OrderPOJO;

import java.util.List;

public class OrderTestDataFactory {

    public static OrderPOJO validOrder(int userId, List<OrderItemPOJO> items) {
        return OrderPOJO.builder()
                .userId(userId)
                .items(items)
                .build();
    }

    public static OrderPOJO orderWithoutUserId(List<OrderItemPOJO> items) {
        return OrderPOJO.builder()
                .items(items)
                .build();
    }

    public static OrderPOJO invalidOrder(int userId, int productId, int quantity) {
        return OrderPOJO.builder()
                .userId(userId)
                .items(List.of(new OrderItemPOJO(productId, quantity)))
                .build();
    }
}