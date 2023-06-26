package antifraud.persistence;

import antifraud.business.models.suspicious.cards.StolenCardEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StolenCardRepository extends CrudRepository<StolenCardEntity, Long> {
    boolean existsByNumber(String number);
    List<StolenCardEntity> findByOrderById();
    void deleteByNumber(String number);
}