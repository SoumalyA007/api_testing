package payloads.response;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponsePOJO {

    private int id;
    private String email;
    private String username;
    private String role;
    private UserDetailsResponsePOJO details;
}
