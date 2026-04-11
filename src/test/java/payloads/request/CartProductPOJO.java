package payloads.request;

import lombok.*;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartProductPOJO {

    private int productId;
    private int quantity;
}