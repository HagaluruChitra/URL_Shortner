package com.chitra.urlshortener.config;

import java.time.Instant;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.chitra.urlshortener.auth.JwtAuthenticationFilter;
import com.chitra.urlshortener.auth.RateLimitFilter;
import com.chitra.urlshortener.auth.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {
    @SuppressWarnings("unused")
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @SuppressWarnings("unused")
    @Bean
    DaoAuthenticationProvider authenticationProvider(UserDetailsServiceImpl users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @SuppressWarnings("unused")
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @SuppressWarnings("unused")
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                           RateLimitFilter rateLimitFilter, DaoAuthenticationProvider provider,
                                           ObjectMapper objectMapper) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/{shortCode}").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    objectMapper.writeValue(response.getOutputStream(), Map.of("timestamp", Instant.now(), "status", 401,
                            "error", "Unauthorized", "code", "UNAUTHORIZED", "message", "Authentication is required.", "path", request.getRequestURI()));
                }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}