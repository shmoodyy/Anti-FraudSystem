package antifraud.persistence;

import antifraud.business.models.suspicious.ips.SuspiciousIpEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuspiciousIpRepository extends CrudRepository<SuspiciousIpEntity, Long> {
    boolean existsByIp(String ip);
    List<SuspiciousIpEntity> findByOrderById();
    void deleteByIp(String ip);
}