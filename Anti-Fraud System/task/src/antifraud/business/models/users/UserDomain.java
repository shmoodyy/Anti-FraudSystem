package antifraud.business.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDomain {
    private Long id;
    private String name;
    private String username;

    @JsonIgnore
    private String password;

    private String role;

    @JsonIgnore
    private boolean isActive;
}