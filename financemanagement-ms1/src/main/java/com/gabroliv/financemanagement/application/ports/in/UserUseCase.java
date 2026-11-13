package com.gabroliv.financemanagement.application.ports.in;

import com.gabroliv.financemanagement.application.command.CreateUserCommand;
import com.gabroliv.financemanagement.application.command.UpdateUserCommand;
import com.gabroliv.financemanagement.application.command.UserImportItem;
import com.gabroliv.financemanagement.application.result.BulkUserImportResult;
import com.gabroliv.financemanagement.domain.model.User;

import java.util.List;

public interface UserUseCase {

    User create(CreateUserCommand command);

    BulkUserImportResult createUsers(List<UserImportItem> items);

    boolean existsById(Long id);

    List<User> findAll(Long authenticatedUserId);

    User findById(Long authenticatedUserId, Long id);

    User update(Long authenticatedUserId, Long id, UpdateUserCommand command);

    void deleteById(Long authenticatedUserId, Long id);
}
