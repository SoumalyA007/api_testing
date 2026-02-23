package payloads.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartProductResponsePOJO {

    private int productId;
    private int quantity;
}