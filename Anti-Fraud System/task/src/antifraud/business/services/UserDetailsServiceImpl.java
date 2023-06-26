package antifraud.business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import antifraud.business.models.users.UserDetailsImpl;
import antifraud.business.models.users.UserEntity;
import antifraud.persistence.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsernameIgnoreCase(username);

        if (user != null) {
            return new UserDetailsImpl(user);
        }
        throw new UsernameNotFoundException("Not found: " + username);
    }
}
