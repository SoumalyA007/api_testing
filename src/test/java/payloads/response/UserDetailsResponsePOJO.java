package payloads.response;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponsePOJO {

    private String firstname;
    private String lastname;
}
