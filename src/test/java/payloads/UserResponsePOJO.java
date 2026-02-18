package payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponsePOJO {

    private int id;
    private String username;
    private String email;
    private String password;

}
