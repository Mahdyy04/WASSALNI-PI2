package com.example.authentication.controller;

import com.example.authentication.dto.CreateAccountRequest;
import com.example.authentication.entities.AppUser;
import com.example.authentication.service.AuthenticationService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/createAccount")
    public AppUser createAccount(@RequestBody CreateAccountRequest request) {
        return authenticationService.createAccount(request);
    }
// Dans AuthenticationController.java
@PostMapping("/authenticate")
public ResponseEntity<Map<String, Object>> authenticate(@RequestBody Map<String, String> payload) {
    String email = payload.get("email");
    String password = payload.get("password");
    
    boolean success = authenticationService.authenticate(email, password);
    if (success) {
        AppUser user = authenticationService.getUserByEmail(email);
        String token = "jwt-token-" + user.getId(); // À remplacer par un vrai JWT
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("token", token);
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    } else {
        return ResponseEntity.status(401).body(Map.of("status", "failure"));
    }
}

    @GetMapping("/users")
    public List<AppUser> getAllUsers() {
        return authenticationService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    public AppUser getUserById(@PathVariable String userId) {
        return authenticationService.getUserById(userId);
    }

    @GetMapping("/users/email/{email}")
    public AppUser getUserByEmail(@PathVariable String email) {
        return authenticationService.getUserByEmail(email);
    }

    @PutMapping("/users/{userId}/ban")
    public AppUser banUser(@PathVariable String userId) {
        return authenticationService.banUser(userId);
    }

    @PutMapping("/users/{userId}/unban")
    public AppUser unbanUser(@PathVariable String userId) {
        return authenticationService.unbanUser(userId);
    }
}