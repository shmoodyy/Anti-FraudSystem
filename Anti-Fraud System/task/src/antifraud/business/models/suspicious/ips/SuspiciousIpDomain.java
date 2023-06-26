package antifraud.business.models.suspicious.ips;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuspiciousIpDomain {
    private Long id;
    private String ip;
}
