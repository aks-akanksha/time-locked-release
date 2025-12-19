package com.example.timelock.api;

import com.example.timelock.security.jwt.JwtService;
import com.example.timelock.repo.UserRepository;         
import com.example.timelock.entity.User;                 
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository users;

    public AuthController(AuthenticationManager authManager, JwtService jwtService, UserRepository users) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.users = users;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String role = users.findByEmailAndActiveTrue(request.getEmail())
                    .map(User::getRole)
                    .orElse("USER");

            String token = jwtService.issue(request.getEmail(), role);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    public static class LoginRequest {
        private String email;
        private String password;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
