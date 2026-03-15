package helpers;

import endpoints.Carts;
import endpoints.Products;
import enums.UserRole;
import org.testng.Assert;
import payloads.request.OrderItemPOJO;
import payloads.request.OrderPOJO;
import payloads.response.OrderItemResponsePOJO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderHelper {

    public static List<OrderItemPOJO> getCartProducts(UserRole role) {

        return Carts.getCarts(role)
                .then()
                .extract()
                .jsonPath()
                .getList("[0].products", OrderItemPOJO.class);
    }

    public static OrderPOJO buildOrder(int userId, List<OrderItemPOJO> items) {

        return OrderPOJO.builder()
                .userId(userId)
                .items(items)
                .build();
    }

    public static float calculateTotal(List<OrderItemResponsePOJO> items) {

        float total = 0;

        for (OrderItemResponsePOJO item : items) {

            float price = Products
                    .getProductById(item.getProductId(), UserRole.USER)
                    .then()
                    .extract()
                    .path("price");

            total += price * item.getQuantity();
        }

        return total;
    }

    public static void validateOrderedItems(
            List<OrderItemPOJO> requestItems,
            List<OrderItemResponsePOJO> responseItems) {

        Map<Integer, Integer> requestMap = requestItems.stream()
                .collect(Collectors.toMap(
                        OrderItemPOJO::getProductId,
                        OrderItemPOJO::getQuantity
                ));

        for (OrderItemResponsePOJO item : responseItems) {

            Assert.assertTrue(
                    requestMap.containsKey(item.getProductId()),
                    "Unexpected productId in response: " + item.getProductId()
            );

            Assert.assertEquals(
                    item.getQuantity(),
                    requestMap.get(item.getProductId()),
                    "Quantity mismatch for productId " + item.getProductId()
            );
        }
    }
}