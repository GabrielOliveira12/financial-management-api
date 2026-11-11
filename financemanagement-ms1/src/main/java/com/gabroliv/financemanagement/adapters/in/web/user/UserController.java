package com.gabroliv.financemanagement.adapters.in.web.user;

import com.gabroliv.financemanagement.adapters.in.web.user.dto.UpdateUserRequest;
import com.gabroliv.financemanagement.adapters.in.web.user.dto.UserRequest;
import com.gabroliv.financemanagement.adapters.in.web.user.dto.UserResponse;
import com.gabroliv.financemanagement.application.command.CreateUserCommand;
import com.gabroliv.financemanagement.application.command.UpdateUserCommand;
import com.gabroliv.financemanagement.application.ports.in.UserUseCase;
import com.gabroliv.financemanagement.domain.model.User;
import com.gabroliv.financemanagement.adapters.in.security.AuthenticatedUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        User user = userUseCase.create(
                new CreateUserCommand(
                        request.name(),
                        request.email(),
                        request.username(),
                        request.password()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUser) {
        List<UserResponse> users = userUseCase.findAll(authenticatedUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUser, @PathVariable Long id) {
        return ResponseEntity.ok(toResponse(userUseCase.findById(authenticatedUser.getId(), id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUser,
                                               @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        User user = userUseCase.update(
                authenticatedUser.getId(),
                id,
                new UpdateUserCommand(
                        request.name(),
                        request.email(),
                        request.username(),
                        request.password()
                )
        );

        return ResponseEntity.ok(toResponse(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@AuthenticationPrincipal AuthenticatedUserDetails authenticatedUser, @PathVariable Long id) {
        userUseCase.deleteById(authenticatedUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUsername()
        );
    }
}
