package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.application.command.CreateUserCommand;
import com.gabroliv.financemanagement.application.command.UpdateUserCommand;
import com.gabroliv.financemanagement.application.port.out.UserRepositoryPort;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void shouldCreateUserWhenEmailAndUsernameDoNotExist() {
        CreateUserCommand command = new CreateUserCommand(
                "Test Name",
                "test@example.com",
                "testuser",
                "testpassord"
        );

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

        User foundUser = userService.findById(1L);

        assertEquals(1L, foundUser.getId());
        assertEquals("Test Name", foundUser.getName());

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.findById(99L)
        );

        verify(userRepository).findById(99L);
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = List.of(
                createUser(1L),
                new User(
                        2L,
                        "Ana Silva",
                        "ana@example.com",
                        "ana",
                        "password456"
                )
        );

        when(userRepository.findAll()).thenReturn(users);

        List<User> foundUsers = userService.findAll();

        assertEquals(2, foundUsers.size());
        assertEquals("Test Name", foundUsers.get(0).getName());
        assertEquals("Ana Silva", foundUsers.get(1).getName());

        verify(userRepository).findAll();
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

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.update(1L, command);

        assertEquals(1L, updatedUser.getId());
        assertEquals("Test Name Updated", updatedUser.getName());
        assertEquals("test@example.com", updatedUser.getEmail());
        assertEquals("testuser", updatedUser.getUsername());
        assertEquals("newtestpassord", updatedUser.getPassword());

        verify(userRepository).findById(1L);
        verify(userRepository).save(existingUser);
    }

    @Test
    void shouldDeleteUserWhenUserExists() {
        User user = createUser(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteById(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteUserWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteById(99L)
        );

        verify(userRepository).findById(99L);
        verify(userRepository, never()).deleteById(any());
    }

    private User createUser(Long id) {
        return new User(
                id,
                "Test Name",
                "Test@example.com",
                "testuser",
                "test password"
        );
    }
}
