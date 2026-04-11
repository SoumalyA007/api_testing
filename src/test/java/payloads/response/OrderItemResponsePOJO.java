package payloads.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponsePOJO {

    private int productId;
    private int quantity;
}