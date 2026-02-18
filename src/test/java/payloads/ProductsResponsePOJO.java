package payloads;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductsResponsePOJO {

    private int id;
    private String title;
    private float price;
    private String description;
    private String category;
    private String image;

}
