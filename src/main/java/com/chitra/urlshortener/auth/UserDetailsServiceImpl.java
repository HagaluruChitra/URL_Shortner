package com.chitra.urlshortener.auth;

import com.chitra.urlshortener.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository users;

    public UserDetailsServiceImpl(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return users.findByEmail(email.toLowerCase()).map(user -> new AuthenticatedUser(user.getId(), user.getEmail(), user.getPasswordHash()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}