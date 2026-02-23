package payloads.response;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartResponsePOJO {

    private int id;
    private int userId;
    private String date;
    private List<CartProductResponsePOJO> products;
}