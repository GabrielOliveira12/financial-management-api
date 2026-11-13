package com.gabroliv.financemanagement.adapters.in.web.user;

import com.gabroliv.financemanagement.adapters.in.web.user.dto.CsvImportResponse;
import com.gabroliv.financemanagement.adapters.in.web.user.exception.CsvImportException;
import com.gabroliv.financemanagement.application.command.CreateUserCommand;
import com.gabroliv.financemanagement.application.command.UserImportItem;
import com.gabroliv.financemanagement.application.ports.in.UserUseCase;
import com.gabroliv.financemanagement.application.result.BulkUserImportResult;
import com.gabroliv.financemanagement.domain.model.User;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCsvImportControllerTest {

    @Mock
    private UserUseCase userUseCase;

    private Validator validator;
    private UserCsvImportController controller;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        controller = new UserCsvImportController(userUseCase, validator);
    }

    @Test
    void shouldImportAllValidLinesSuccessfully() {
        String csvContent = "name,email,username,password\n"
                + "Isabela Martins,isabela.martins@example.com,isabela.martins,senha123\n"
                + "Karina Dias,karina.dias@example.com,karina.dias,senha123\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "usuarios.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userUseCase.createUsers(any())).thenAnswer(invocation -> {
            List<UserImportItem> items = invocation.getArgument(0);
            List<User> created = items.stream()
                    .map(item -> new User(1L, item.command().name(), item.command().email(), item.command().username(), "hashed"))
                    .toList();
            return new BulkUserImportResult(created, List.of());
        });

        ResponseEntity<CsvImportResponse> response = controller.importCsv(file);

        assertEquals(2, response.getBody().createdUsers());
        assertEquals(0, response.getBody().failedUsers());
        assertTrue(response.getBody().errors().isEmpty());
    }

    @Test
    void shouldReportLineErrorForInvalidEmailWithoutDroppingValidLines() {
        String csvContent = "name,email,username,password\n"
                + "Isabela Martins,isabela.martins@example.com,isabela.martins,senha123\n"
                + "Joao Pedro,email-invalido,joao.pedro,senha123\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "usuarios.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userUseCase.createUsers(any())).thenAnswer(invocation -> {
            List<UserImportItem> items = invocation.getArgument(0);
            List<User> created = items.stream()
                    .map(item -> new User(1L, item.command().name(), item.command().email(), item.command().username(), "hashed"))
                    .toList();
            return new BulkUserImportResult(created, List.of());
        });

        ResponseEntity<CsvImportResponse> response = controller.importCsv(file);

        assertEquals(1, response.getBody().createdUsers());
        assertEquals(1, response.getBody().failedUsers());
        assertTrue(response.getBody().errors().containsKey("line 3"));
    }

    @Test
    void shouldReportDuplicateEmailAsLineErrorFromUserService() {
        String csvContent = "name,email,username,password\n"
                + "Isabela Martins,isabela.martins@example.com,isabela.martins,senha123\n"
                + "Luis Fernando,isabela.martins@example.com,luis.fernando,senha123\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "usuarios.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userUseCase.createUsers(any())).thenReturn(new BulkUserImportResult(
                List.of(new User(1L, "Isabela Martins", "isabela.martins@example.com", "isabela.martins", "hashed")),
                List.of(new BulkUserImportResult.ImportError(
                        "line 3", "User already exists with email: isabela.martins@example.com"
                ))
        ));

        ResponseEntity<CsvImportResponse> response = controller.importCsv(file);

        assertEquals(1, response.getBody().createdUsers());
        assertEquals(1, response.getBody().failedUsers());
        assertEquals(
                "User already exists with email: isabela.martins@example.com",
                response.getBody().errors().get("line 3")
        );
    }

    @Test
    void shouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "usuarios.csv", "text/csv", new byte[0]);

        assertThrows(CsvImportException.class, () -> controller.importCsv(file));
    }

    @Test
    void shouldThrowExceptionWhenHeadersAreInvalid() {
        String csvContent = "nome,email,usuario,senha\n"
                + "Isabela Martins,isabela.martins@example.com,isabela.martins,senha123\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "usuarios.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(CsvImportException.class, () -> controller.importCsv(file));
    }

    @Test
    void shouldThrowExceptionWhenFileHasOnlyHeaderWithoutAnyLine() {
        String csvContent = "name,email,username,password\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "usuarios.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(CsvImportException.class, () -> controller.importCsv(file));
    }

    @Test
    void shouldReportInconsistentLineAsFieldError() {
        String csvContent = "name,email,username,password\n"
                + "Isabela Martins,isabela.martins@example.com,isabela.martins\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "usuarios.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userUseCase.createUsers(any())).thenReturn(new BulkUserImportResult(List.of(), List.of()));

        ResponseEntity<CsvImportResponse> response = controller.importCsv(file);

        assertEquals(0, response.getBody().createdUsers());
        assertEquals(1, response.getBody().failedUsers());
        assertTrue(response.getBody().errors().containsKey("line 2"));
    }
}
