package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.application.command.CreateUserCommand;
import com.gabroliv.financemanagement.application.command.UpdateUserCommand;
import com.gabroliv.financemanagement.application.command.UserImportItem;
import com.gabroliv.financemanagement.application.ports.in.UserUseCase;
import com.gabroliv.financemanagement.application.ports.out.PasswordHashPort;
import com.gabroliv.financemanagement.application.ports.out.UserRepositoryPort;
import com.gabroliv.financemanagement.application.result.BulkUserImportResult;
import com.gabroliv.financemanagement.domain.exception.UserAccessDeniedException;
import com.gabroliv.financemanagement.domain.exception.UserAlreadyExistsException;
import com.gabroliv.financemanagement.domain.exception.UserNotFoundException;
import com.gabroliv.financemanagement.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHashPort passwordEncoder;

    public UserService(UserRepositoryPort userRepository, PasswordHashPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User create(CreateUserCommand command) {

        String normalizedEmail = normalizeEmail(command.email());
        validateEmailAndUsername(normalizedEmail, command.username(), null);

        String passwordEncoded = passwordEncoder.hash(command.password());
        User user = new User(
                null,
                command.name(),
                normalizedEmail,
                command.username(),
                passwordEncoded
        );

        return userRepository.save(user);
    }

    @Override
    public BulkUserImportResult createUsers(List<UserImportItem> items) {
        List<User> createdUsers = new ArrayList<>();
        List<BulkUserImportResult.ImportError> errors = new ArrayList<>();

        for (UserImportItem item : items) {
            try {
                createdUsers.add(create(item.command()));
            } catch (RuntimeException exception) {
                errors.add(new BulkUserImportResult.ImportError(item.identifier(), exception.getMessage()));
            }
        }

        return new BulkUserImportResult(createdUsers, errors);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public List<User> findAll(Long authenticatedUserId) {
        return List.of(findUserById(authenticatedUserId));
    }

    @Override
    public User findById(Long authenticatedUserId, Long id) {
        validateUserAccess(authenticatedUserId, id);

        return findUserById(id);
    }

    @Override
    public User update(Long authenticatedUserId, Long id, UpdateUserCommand command) {
        validateUserAccess(authenticatedUserId, id);

        User user = findUserById(id);

        String normalizedEmail = normalizeEmail(command.email());

        validateEmailAndUsername(normalizedEmail, command.username(), user);

        user.update(command.name(), normalizedEmail, command.username());

        if (command.password() != null) {
            String passwordEncoded = passwordEncoder.hash(command.password());
            user.updatePassword(passwordEncoded);
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long authenticatedUserId, Long id) {
        validateUserAccess(authenticatedUserId, id);

        User user = findUserById(id);
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

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateUserAccess(Long authenticatedUserId, Long id) {
        if (!authenticatedUserId.equals(id)) {
            throw new UserAccessDeniedException();
        }
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
