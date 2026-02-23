package payloads.response;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryResponsePOJO {

    private int id;
    private int productId;
    private int stockCount;
    private String warehouse;
    private int minThreshold;
}