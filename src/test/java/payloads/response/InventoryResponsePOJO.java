package payloads.response;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryResponsePOJO {

    private Long id;
    private int productId;
    private int quantity;
    private String warehouse;
    private int minThreshold;
}