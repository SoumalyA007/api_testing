package payloads.request;

import lombok.*;

import java.util.List;
import enums.OrderStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPOJO {


    private int userId;
    private List<OrderItemPOJO> items;

}