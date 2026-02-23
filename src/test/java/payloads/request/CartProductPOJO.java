package payloads.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartProductPOJO {

    private int productId;
    private int quantity;
}