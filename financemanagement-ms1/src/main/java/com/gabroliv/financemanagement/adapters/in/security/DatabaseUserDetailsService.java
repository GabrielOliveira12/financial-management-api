package com.gabroliv.financemanagement.adapters.in.security;

import com.gabroliv.financemanagement.application.ports.out.UserRepositoryPort;
import com.gabroliv.financemanagement.domain.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepository;

    public DatabaseUserDetailsService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = identifier.contains("@")
                ? userRepository.findByEmail(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier))
                : userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));

        return new AuthenticatedUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword()
        );
    }
}
