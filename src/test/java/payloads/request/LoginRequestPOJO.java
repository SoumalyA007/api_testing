package payloads.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestPOJO {

    private String email;
    private String password;
}