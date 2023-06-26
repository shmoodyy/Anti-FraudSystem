package antifraud.business.models.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserIdDTO {
    private Long id;
    private String name;
    private String username;
    private String role;
}