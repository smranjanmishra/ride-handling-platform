package com.zeta.rider_service.security;

import com.zeta.rider_service.entity.Rider;
import com.zeta.rider_service.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final RiderRepository riderRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {

        Rider rider;

        // Login ke time email aata hai
        if (username.contains("@")) {
            rider = riderRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }
        // JWT filter ke time riderId aata hai
        else {
            Long id = Long.parseLong(username);
            rider = riderRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }

        return new org.springframework.security.core.userdetails.User(
                rider.getEmail(),
                rider.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_RIDER"))
        );
    }

}
