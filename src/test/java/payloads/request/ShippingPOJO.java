package payloads.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingPOJO {

    private int id;
    private int orderId;
    private String trackingNumber;
    private String carrier;
    private String estimatedDelivery;
}