package payloads.response;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import enums.OrderStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponsePOJO {

    private int id;
    private int userId;
    private List<OrderItemResponsePOJO> items;
    private double totalPrice;
    private OrderStatus status;
    private String orderDate;
}