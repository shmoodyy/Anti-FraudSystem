package antifraud.business.services;

import antifraud.business.models.users.UserIdDomain;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import antifraud.business.models.users.UserDomain;
import antifraud.business.models.users.UserEntity;
import antifraud.persistence.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ModelMapper serviceModelMapper;

    public UserIdDomain registerUser(UserDomain userDomain) {
        var entity = convertUserDomainToEntity(userDomain);
        userRepository.save(entity);
        return convertEntityToIdDomain(entity);
    }

    public List<UserDomain> listUsers() {
        return userRepository.findByOrderById().stream()
                .map(this::convertUserEntityToDomain)
                .toList();
    }

    @Transactional
    public Map<String, String> deleteByUsername(String username) {
        userRepository.deleteByUsername(username);
        Map<String, String> deleteMsg = new ConcurrentHashMap<>(2);
        deleteMsg.put("username", username);
        deleteMsg.put("status", "Deleted successfully!");
        return deleteMsg;
    }

    @Transactional
    public UserIdDomain updateUserRole(String username, String role) {
        var user = userRepository.findByUsernameIgnoreCase(username);
        user.setRole(role);
        return convertEntityToIdDomain(user);
    }

    @Transactional
    public Map<String, String> updateUserAccess(String username, String operation) {
        var user = userRepository.findByUsernameIgnoreCase(username);
        String status = null;
        switch (operation.toUpperCase()) {
            case "LOCK"   -> {
                user.setActive(false);
                status = "locked";
            }
            case "UNLOCK" -> {
                user.setActive(true);
                status = "unlocked";
            }
        }
        Map<String, String> statusMap = new ConcurrentHashMap<>(1);
        statusMap.put("status", "User " + username + " " + status + "!");
        return statusMap;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public UserDomain findByUsernameIgnoreCase(String username) {
        return convertUserEntityToDomain(userRepository.findByUsernameIgnoreCase(username));
    }

    // Service utility methods
    public UserDomain convertUserEntityToDomain(UserEntity userEntity) {
        return serviceModelMapper.map(userEntity, UserDomain.class);
    }

    public UserEntity convertUserDomainToEntity(UserDomain userDomain) {
        return serviceModelMapper.map(userDomain, UserEntity.class);
    }

    public UserIdDomain convertEntityToIdDomain(UserEntity userEntity) {
        return serviceModelMapper.map(userEntity, UserIdDomain.class);
    }
}