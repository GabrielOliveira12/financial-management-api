package com.gabroliv.financemanagement.application.command;

public record CreateUserCommand(String name, String email, String username, String password) {}
