package payloads.request;
import lombok.Builder;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPOJO {

    private int id;
    private int productId;
    private int quantity;
    private String warehouse;
    private int minThreshold;
}