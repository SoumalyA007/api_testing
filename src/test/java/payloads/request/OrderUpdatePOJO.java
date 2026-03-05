package payloads.request;

import enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import payloads.response.OrderItemResponsePOJO;

import java.util.List;

@Builder
@Data
public class OrderUpdatePOJO {

    private int id;
    private int userId;
    private List<OrderItemResponsePOJO> items;
    private double totalAmount;
    private OrderStatus status;
    private String orderDate;
}
