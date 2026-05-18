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

    @GetMapping("/profile")
    public ResponseEntity<User> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userService.getMyProfile(principal.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateMyProfile(Principal principal, @RequestBody User requestBody) {
        return ResponseEntity.ok(userService.updateMyProfile(
                principal.getName(),
                requestBody.getFirstName(),
                requestBody.getLastName(),
                requestBody.getEmail()
        ));
    }

    @GetMapping("/my-projects")
    public ResponseEntity<?> getMyProjects(Principal principal) {
        return ResponseEntity.ok(userService.getMyProjects(principal.getName()));
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

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> modifyUserRole(@PathVariable Long id, @RequestParam String role) {
        String responseMessage = userService.modifyUserRole(id, role);
        return ResponseEntity.ok(responseMessage);
    }
}