package payloads.request;
import lombok.Builder;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestPOJO {

    private String username;
    private String password;
}