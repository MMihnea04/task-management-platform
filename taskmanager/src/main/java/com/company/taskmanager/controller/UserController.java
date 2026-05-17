package com.company.taskmanager.controller;

import com.company.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Endpoint pentru promovare la Admin
    // URL complet: PATCH http://localhost:8080/api/users/{username}/promote
    @PatchMapping("/{username}/promote")
    @PreAuthorize("hasRole('ADMIN')") // exclusiv apelat de alti admini
    public ResponseEntity<String> promoteToAdmin(@PathVariable String username) {
        String responseMessage = userService.promoteUserToAdmin(username);
        return ResponseEntity.ok(responseMessage);
    }
}