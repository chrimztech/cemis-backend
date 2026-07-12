package zm.unza.tels.cemis.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import zm.unza.tels.cemis.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(CemisUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public CemisUserDetails loadByUserId(String id) {
        return userRepository.findById(UUID.fromString(id))
                .map(CemisUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
    }
}
