package payloads.request;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPOJO {

    private String id;
    private String name;
}
