package payloads.request;
import lombok.Builder;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemPOJO {

    private Integer  productId;
    private Integer  quantity;

}