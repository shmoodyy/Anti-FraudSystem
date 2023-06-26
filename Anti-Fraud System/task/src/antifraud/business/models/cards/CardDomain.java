package antifraud.business.models.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDomain {
    private Long id;
    private String number;
    private Long allowedLimit = 200L;
    private Long manualProcessingLimit = 1500L;
}