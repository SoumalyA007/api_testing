package payloads.request;

import lombok.Builder;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPOJO {

    private String id;
    private String name;
}
