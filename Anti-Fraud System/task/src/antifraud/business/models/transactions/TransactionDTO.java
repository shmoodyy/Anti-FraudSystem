package antifraud.business.models.transactions;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    @Min(1)
    @NotNull
    private Long amount;

    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
    @NotEmpty
    private String ip;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TransactionRegions region;

    private LocalDateTime date = LocalDateTime.now();

    @Pattern(regexp = "^\\d{16}$")
    @NotEmpty
    private String number;
}