package com.gabroliv.financemanagement.adapters.in.web.user;

import com.gabroliv.financemanagement.adapters.in.web.user.dto.CsvImportResponse;
import com.gabroliv.financemanagement.adapters.in.web.user.dto.UserRequest;
import com.gabroliv.financemanagement.adapters.in.web.user.exception.CsvImportException;
import com.gabroliv.financemanagement.application.command.CreateUserCommand;
import com.gabroliv.financemanagement.application.command.UserImportItem;
import com.gabroliv.financemanagement.application.ports.in.UserUseCase;
import com.gabroliv.financemanagement.application.result.BulkUserImportResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserCsvImportController {

    private static final Set<String> REQUIRED_HEADERS = Set.of("name", "email", "username", "password");

    private final UserUseCase userUseCase;
    private final Validator validator;

    public UserCsvImportController(UserUseCase userUseCase, Validator validator) {
        this.userUseCase = userUseCase;
        this.validator = validator;
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CsvImportResponse> importCsv(@RequestParam("file") MultipartFile file) {
        ParsedCsv parsedCsv = parseFile(file);

        BulkUserImportResult result = userUseCase.createUsers(parsedCsv.validItems());

        Map<String, String> errors = new LinkedHashMap<>(parsedCsv.fieldErrors());
        result.errors().forEach(error -> errors.putIfAbsent(error.identifier(), error.reason()));

        CsvImportResponse response = new CsvImportResponse(result.createdUsers().size(), errors.size(), errors);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private ParsedCsv parseFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CsvImportException("CSV import failed.", Map.of("file", "CSV file must not be empty."));
        }

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build().parse(reader)) {

            validateHeaders(parser);

            List<UserImportItem> validItems = new ArrayList<>();
            Map<String, String> fieldErrors = new LinkedHashMap<>();
            boolean hasAnyLine = false;

            for (CSVRecord record : parser) {
                hasAnyLine = true;
                long lineNumber = record.getRecordNumber() + 1;
                String identifier = "line " + lineNumber;

                if (!record.isConsistent()) {
                    fieldErrors.put(identifier, "CSV line has an invalid number of columns.");
                    continue;
                }

                UserRequest request = new UserRequest(record.get("name"), record.get("email"), record.get("username"), record.get("password"));

                List<String> violations = validateUserRequest(request);
                if (!violations.isEmpty()) {
                    fieldErrors.put(identifier, String.join(" ", violations));
                    continue;
                }

                validItems.add(new UserImportItem(
                        identifier,
                        new CreateUserCommand(request.name(), request.email(), request.username(), request.password())
                ));
            }

            if (!hasAnyLine) {
                throw new CsvImportException("CSV import failed.", Map.of("file", "CSV file must contain at least one user."));
            }

            return new ParsedCsv(validItems, fieldErrors);

        } catch (IOException exception) {
            throw new CsvImportException("CSV import failed.", Map.of("file", "Could not read the CSV file."));
        }
    }

    private void validateHeaders(CSVParser parser) {
        Map<String, Integer> headers = parser.getHeaderMap();

        boolean hasInvalidHeaders = headers.size() != REQUIRED_HEADERS.size() || !headers.keySet().containsAll(REQUIRED_HEADERS);

        if (hasInvalidHeaders) {
            throw new CsvImportException("CSV import failed.", Map.of("file", "CSV headers must be: name, email, username, password."));
        }
    }

    private List<String> validateUserRequest(UserRequest request) {
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        return violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
    }

    private record ParsedCsv(List<UserImportItem> validItems, Map<String, String> fieldErrors) {}
}
