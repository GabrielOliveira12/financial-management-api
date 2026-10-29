package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.application.command.CreateUserCommand;
import com.gabroliv.financemanagement.application.command.UpdateUserCommand;
import com.gabroliv.financemanagement.application.port.in.UserUseCase;
import com.gabroliv.financemanagement.application.port.out.UserRepositoryPort;
import com.gabroliv.financemanagement.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;

    public UserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(CreateUserCommand command) {
        validateEmailAndUsername(command.email(), command.username(), null);

        User user = new User(
                null,
                command.name(),
                command.email(),
                command.username(),
                command.password()
        );

        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User update(Long id, UpdateUserCommand command) {
        User user = findById(id);

        validateEmailAndUsername(command.email(), command.username(), user);

        user.update(
                command.name(),
                command.email(),
                command.username(),
                command.password()
        );

        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        User user = findById(id);
        userRepository.deleteById(user.getId());
    }

    private void validateEmailAndUsername(String email, String username, User currentUser) {
        boolean isNewUser = currentUser == null;

        boolean emailChanged = isNewUser || !currentUser.getEmail().equals(email);
        if (emailChanged && userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("email", email);
        }

        boolean usernameChanged = isNewUser || !currentUser.getUsername().equals(username);
        if (usernameChanged && userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("username", username);
        }
    }
}
