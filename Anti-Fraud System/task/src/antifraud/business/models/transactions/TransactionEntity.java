package antifraud.business.models.transactions;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TRANSACTIONS")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column
    private Long amount;

    @Column
    private String ip;

    @Column
    private String number;

    @Column
    @Enumerated(EnumType.STRING)
    private TransactionRegions region;

    @Column
    private LocalDateTime date;

    @Column
    private TransactionStatus result;

    @Column
    private String feedback;
}