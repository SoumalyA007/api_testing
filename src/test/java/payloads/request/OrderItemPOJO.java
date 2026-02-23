package payloads.request;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemPOJO {

    private int productId;
    private int quantity;
}
