package antifraud.persistence;

import antifraud.business.models.cards.CardEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends CrudRepository<CardEntity, Long> {
    CardEntity findByNumber(String number);
    boolean existsByNumber(String number);
}
