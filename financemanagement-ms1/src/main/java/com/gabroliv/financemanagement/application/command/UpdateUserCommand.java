package com.gabroliv.financemanagement.application.command;

public record UpdateUserCommand(String name, String email, String username, String password) { }
