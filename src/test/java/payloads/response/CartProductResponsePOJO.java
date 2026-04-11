package payloads.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartProductResponsePOJO {

    private int productId;
    private int quantity;
}