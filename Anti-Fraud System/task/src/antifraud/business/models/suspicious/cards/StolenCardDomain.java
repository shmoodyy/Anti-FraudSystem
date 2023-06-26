package antifraud.business.models.suspicious.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StolenCardDomain {
    private Long id;
    private String number;
}
