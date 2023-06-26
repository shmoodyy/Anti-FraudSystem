package antifraud.business.models.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFeedbackDTO {
    @JsonProperty("transactionId")
    @NotNull
    private Long id;

    private String feedback;
}