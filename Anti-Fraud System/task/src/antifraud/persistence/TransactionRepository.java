package antifraud.persistence;

import antifraud.business.models.transactions.TransactionEntity;
import antifraud.business.models.transactions.TransactionRegions;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<TransactionEntity, Long> {
    boolean existsById(@NotNull Long id);
    boolean existsByIdAndFeedback(Long id, String feedback);
    List<TransactionEntity> findByOrderById();

    List<TransactionEntity> findByNumberOrderById(String number);

    @Query("SELECT COUNT(DISTINCT t.region) FROM TransactionEntity t " +
            "WHERE t.region <> ?1 AND t.number = ?2 AND t.date BETWEEN ?3 AND ?4")
    Long countUniqueRegionsInLastHour(TransactionRegions region, String number
            , LocalDateTime lastHour, LocalDateTime date);

    @Query("SELECT COUNT(DISTINCT t.ip) FROM TransactionEntity t " +
            "WHERE t.ip <> ?1 AND t.number = ?2 AND t.date BETWEEN ?3 AND ?4")
    Long countUniqueIpsInLastHour(String ip, String number
            , LocalDateTime lastHour, LocalDateTime date);
}