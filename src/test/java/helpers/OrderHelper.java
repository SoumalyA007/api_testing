package helpers;

import endpoints.Carts;
import endpoints.Orders;
import endpoints.Products;
import enums.UserRole;
import payloads.request.OrderItemPOJO;
import payloads.request.OrderPOJO;
import payloads.response.OrderItemResponsePOJO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderHelper {

    // ================= SETUP =================

    public static List<OrderItemPOJO> getCartProducts(UserRole role) {
        return Carts.getCarts(role)
                .then()
                .extract()
                .jsonPath()
                .getList("[0].products", OrderItemPOJO.class);
    }

    public static int createTestOrder(OrderPOJO order, UserRole role) {
        
        return Orders.createOrder(order, role)
                .then()
                .extract()
                .path("id");
    }

    // ================= VALIDATION =================

    public static double calculateTotal(List<OrderItemResponsePOJO> items) {
        return items.stream()
                .mapToDouble(item -> {
                    double price = Products
                            .getProductById(item.getProductId(), UserRole.USER)
                            .then()
                            .extract()
                            .jsonPath()
                            .getDouble("price");

                    return price * item.getQuantity();
                })
                .sum();
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

            if (!requestMap.containsKey(item.getProductId())) {
                throw new AssertionError("Unexpected productId: " + item.getProductId());
            }

            if (item.getQuantity() != requestMap.get(item.getProductId())) {
                throw new AssertionError("Quantity mismatch for productId: " + item.getProductId());
            }
        }
    }

    public static void deleteOrderIfExists(int orderId) {
        try {
            if(orderId != 0){
                Orders.deleteOrder(orderId, UserRole.ADMIN);
            }
        } catch (Exception ignored) {}
    }

}