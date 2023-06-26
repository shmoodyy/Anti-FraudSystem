package antifraud.business.models.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder("transactionId")
public class TransactionDomain {
    @JsonProperty("transactionId")
    private Long id;
    private Long amount;
    private String ip;
    private String number;
    private TransactionRegions region;
    private LocalDateTime date;
    private TransactionStatus result;
    private String feedback = "";
}