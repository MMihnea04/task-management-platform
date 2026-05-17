package com.company.taskmanager.controller;

import com.company.taskmanager.entity.User;
import com.company.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userService.getMyProfile(principal.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(
            Principal principal,
            @RequestParam String firstName,
            @RequestParam String lastName
    ) {
        return ResponseEntity.ok(userService.updateMyProfile(principal.getName(), firstName, lastName));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("Utilizatorul a fost dezactivat cu succes!");
    }

    @PatchMapping("/{username}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> promoteToAdmin(@PathVariable String username) {
        String responseMessage = userService.promoteUserToAdmin(username);
        return ResponseEntity.ok(responseMessage);
    }
}