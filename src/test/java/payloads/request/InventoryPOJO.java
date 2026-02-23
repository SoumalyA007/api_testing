package payloads.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPOJO {

    private int id;
    private int productId;
    private int stockCount;
    private String warehouse;
    private int minThreshold;
}