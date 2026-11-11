package com.gabroliv.financemanagement.application.ports.out;

public interface PasswordHashPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
