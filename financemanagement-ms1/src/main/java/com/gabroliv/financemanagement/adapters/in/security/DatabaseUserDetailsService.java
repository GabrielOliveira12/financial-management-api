package com.gabroliv.financemanagement.adapters.in.security;

import com.gabroliv.financemanagement.application.port.out.UserRepositoryPort;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new AuthenticatedUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword()
        );
    }
}
