package antifraud.business.models.suspicious.cards;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StolenCardDTO {
    @Pattern(regexp = "^\\d{16}$")
    @NotEmpty
    private String number;
}