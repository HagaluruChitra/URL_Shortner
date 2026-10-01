package com.chitra.urlshortener.auth;

import com.chitra.urlshortener.api.ApiException;
import com.chitra.urlshortener.domain.UserEntity;
import com.chitra.urlshortener.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long expirationMs;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtService jwtService, @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.expirationMs = expirationMs;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw ApiException.conflict("An account with this email already exists.");
        UserEntity user = users.saveAndFlush(new UserEntity(request.name().trim(), email, passwordEncoder.encode(request.password())));
        return response(new AuthenticatedUser(user.getId(), user.getEmail(), user.getPasswordHash()), user.getName());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
            AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
            String displayName = users.findById(principal.id()).map(UserEntity::getName).orElse("");
            return response(principal, displayName);
        } catch (BadCredentialsException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email or password is incorrect.");
        }
    }

    private AuthDtos.AuthResponse response(AuthenticatedUser user, String name) {
        return new AuthDtos.AuthResponse("Bearer", jwtService.issue(user), expirationMs / 1000,
                new AuthDtos.UserResponse(user.id(), name, user.email()));
    }
}