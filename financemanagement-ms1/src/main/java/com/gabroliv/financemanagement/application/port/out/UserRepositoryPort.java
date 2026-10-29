package com.gabroliv.financemanagement.application.port.out;

import com.gabroliv.financemanagement.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    List<User> findAll();

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    void deleteById(Long id);
}
