package payloads.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponsePOJO {

    private int productId;
    private int quantity;
}