package com.gabroliv.financemanagement.application.port.in;

import com.gabroliv.financemanagement.application.command.CreateUserCommand;
import com.gabroliv.financemanagement.application.command.UpdateUserCommand;
import com.gabroliv.financemanagement.domain.model.User;

import java.util.List;

public interface UserUseCase {

    User create(CreateUserCommand command);

    List<User> createUsers(List<CreateUserCommand> commands);

    List<User> findAll(Long authenticatedUserId);

    User findById(Long authenticatedUserId, Long id);

    User update(Long authenticatedUserId, Long id, UpdateUserCommand command);

    void deleteById(Long authenticatedUserId, Long id);
}
