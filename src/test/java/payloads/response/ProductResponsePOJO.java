package payloads.response;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponsePOJO {

    private int id;
    private String title;
    private double price;
    private String category;
    private String description;
    private String image;
}
