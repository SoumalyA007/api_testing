package payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartResponsePOJO {

    private int id;
    private int userId;
    private List<ProductsPOJO> products;



}
