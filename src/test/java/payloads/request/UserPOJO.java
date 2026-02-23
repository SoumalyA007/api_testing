package payloads.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payloads.request.UserDetailsPOJO;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserPOJO {

    private int id;
    private String email;
    private String username;
    private String password;
    private String role;
    private UserDetailsPOJO details;
}
