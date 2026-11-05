package com.gabroliv.financemanagement.adapters.in.web.auth;

import com.gabroliv.financemanagement.adapters.in.security.AuthenticatedUserDetails;
import com.gabroliv.financemanagement.adapters.in.security.JwtService;
import com.gabroliv.financemanagement.adapters.in.web.auth.dto.LoginRequest;
import com.gabroliv.financemanagement.adapters.in.web.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        AuthenticatedUserDetails authenticatedUser = (AuthenticatedUserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(authenticatedUser.getId(), authenticatedUser.getUsername());

        return ResponseEntity.ok(new LoginResponse(token, "Bearer"));
    }
}
