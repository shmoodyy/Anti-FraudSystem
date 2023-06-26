package antifraud.business.models.transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatusDTO {
    private TransactionStatus result;
    private String info;
}