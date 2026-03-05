package payloads.request;


import enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderStatusUpdatePOJO {

    public OrderStatus status;

}
