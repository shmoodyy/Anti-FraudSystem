package antifraud.persistence;

import antifraud.business.models.users.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    UserEntity findByUsernameIgnoreCase(String username);
    boolean existsByUsername(String username);
    List<UserEntity> findByOrderById();
    void deleteByUsername(String username);
}
