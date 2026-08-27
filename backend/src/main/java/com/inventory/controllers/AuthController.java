package com.inventory.controllers;

import com.inventory.models.User;
import com.inventory.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService = AuthService.getInstance();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Username and password required");
        }

        String token = authService.login(username, password);
        if (token != null) {
            User user = authService.getUserByToken(token);
            return ResponseEntity.ok(Map.of("token", token, "user", user));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body("Username and password required");
        }

        boolean created = authService.registerUser(username, password);
        if (created) {
            // Auto-login after registration so the user gets a token right away
            String token = authService.login(username, password);
            User user = authService.getUserByToken(token);
            return ResponseEntity.ok(Map.of("token", token, "user", user));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
        }
    }
}
