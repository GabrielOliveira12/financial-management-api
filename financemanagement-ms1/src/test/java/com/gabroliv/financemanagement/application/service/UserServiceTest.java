package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.application.command.CreateUserCommand;
import com.gabroliv.financemanagement.application.command.UpdateUserCommand;
import com.gabroliv.financemanagement.application.ports.out.PasswordHashPort;
import com.gabroliv.financemanagement.application.ports.out.UserRepositoryPort;
import com.gabroliv.financemanagement.domain.exception.UserAccessDeniedException;
import com.gabroliv.financemanagement.domain.exception.UserAlreadyExistsException;
import com.gabroliv.financemanagement.domain.exception.UserNotFoundException;
import com.gabroliv.financemanagement.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordHashPort passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void shouldCreateUserWhenEmailAndUsernameDoNotExist() {
        CreateUserCommand command = new CreateUserCommand(
                "Test Name",
                "test@example.com",
                "testuser",
                "testpassord"
        );

        String hashedPassword = "$2a$10$hashedPasswordExample";

        when(passwordEncoder.hash(command.password())).thenReturn(hashedPassword);
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(userRepository.existsByUsername(command.username())).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);

            return new User(
                    1L,
                    user.getName(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getPassword()
            );
        });

        User createdUser = userService.create(command);

        assertNotNull(createdUser);
        assertEquals(1L, createdUser.getId());
        assertEquals("Test Name", createdUser.getName());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("testuser", createdUser.getUsername());
        assertEquals(hashedPassword, createdUser.getPassword());

        verify(passwordEncoder).hash("testpassord");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldNotCreateUserWhenEmailAlreadyExists() {
        CreateUserCommand command = new CreateUserCommand(
                "Test Name",
                "test@example.com",
                "testuser",
                "testpassord"
        );

        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.create(command)
        );

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).existsByUsername(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldNotCreateUserWhenNormalizedEmailAlreadyExists() {
        CreateUserCommand command = new CreateUserCommand(
                "Test Name",
                "TEST@EXAMPLE.COM",
                "testuser",
                "testpassord"
        );

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.create(command)
        );

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).existsByUsername(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldNotCreateUserWhenUsernameAlreadyExists() {
        CreateUserCommand command = new CreateUserCommand(
                "Test Name",
                "test@example.com",
                "testuser",
                "testpassord"
        );

        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(userRepository.existsByUsername(command.username())).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.create(command)
        );

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldFindUserByIdWhenUserExists() {
        User user = createUser(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.findById(1L, 1L);

        assertEquals(1L, foundUser.getId());
        assertEquals("Test Name", foundUser.getName());

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.findById(99L, 99L)
        );

        verify(userRepository).findById(99L);
    }

    @Test
    void shouldThrowAccessDeniedWhenFindingAnotherUser() {
        Long authenticatedUserId = 1L;
        Long requestedUserId = 2L;

        assertThrows(
                UserAccessDeniedException.class,
                () -> userService.findById(authenticatedUserId, requestedUserId)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldReturnOnlyAuthenticatedUser() {
        Long authenticatedUserId = 1L;
        User authenticatedUser = createUser(authenticatedUserId);

        when(userRepository.findById(authenticatedUserId))
                .thenReturn(Optional.of(authenticatedUser));

        List<User> foundUsers = userService.findAll(authenticatedUserId);

        assertEquals(1, foundUsers.size());
        assertEquals(authenticatedUserId, foundUsers.get(0).getId());
        assertEquals("Test Name", foundUsers.get(0).getName());

        verify(userRepository).findById(authenticatedUserId);
        verify(userRepository, never()).findAll();
    }

    @Test
    void shouldUpdateUserWhenUserExists() {
        User existingUser = createUser(1L);

        UpdateUserCommand command = new UpdateUserCommand(
                "Test Name Updated",
                "test@example.com",
                "testuser",
                "newtestpassord"
        );
        String hashedPassword = "$2a$10$hashedNewPasswordExample";

        when(passwordEncoder.hash(command.password()))
                .thenReturn(hashedPassword);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.update(1L, 1L, command);

        assertEquals(1L, updatedUser.getId());
        assertEquals("Test Name Updated", updatedUser.getName());
        assertEquals("test@example.com", updatedUser.getEmail());
        assertEquals("testuser", updatedUser.getUsername());
        assertEquals(hashedPassword, updatedUser.getPassword());

        verify(passwordEncoder).hash("newtestpassord");
        verify(userRepository).findById(1L);
        verify(userRepository).save(existingUser);
    }

    @Test
    void shouldKeepCurrentPasswordWhenUpdatingUserWithNullPassword() {
        User existingUser = createUser(1L);

        UpdateUserCommand command = new UpdateUserCommand(
                "Test Name Updated",
                "test@example.com",
                "testuser",
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.update(1L, 1L, command);

        assertEquals("test password", updatedUser.getPassword());

        verify(userRepository).findById(1L);
        verify(userRepository).save(existingUser);
        verify(passwordEncoder, never()).hash(any());
    }

    @Test
    void shouldThrowAccessDeniedWhenUpdatingAnotherUser() {
        UpdateUserCommand command = new UpdateUserCommand(
                "Another Name",
                "another@example.com",
                "anotheruser",
                null
        );

        assertThrows(
                UserAccessDeniedException.class,
                () -> userService.update(1L, 2L, command)
        );

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldDeleteUserWhenUserExists() {
        User user = createUser(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteById(1L, 1L);

        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteUserWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteById(99L, 99L)
        );

        verify(userRepository).findById(99L);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowAccessDeniedWhenDeletingAnotherUser() {
        assertThrows(
                UserAccessDeniedException.class,
                () -> userService.deleteById(1L, 2L)
        );

        verifyNoInteractions(userRepository);
    }

    private User createUser(Long id) {
        return new User(
                id,
                "Test Name",
                "test@example.com",
                "testuser",
                "test password"
        );
    }
}
