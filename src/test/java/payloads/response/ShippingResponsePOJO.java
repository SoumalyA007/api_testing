package payloads.response;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingResponsePOJO {

    private int id;
    private int orderId;
    private String trackingNumber;
    private String carrier;
    private String estimatedDelivery;
}