package com.company.taskmanager.controller;

import com.company.taskmanager.dto.ProjectRequest;
import com.company.taskmanager.entity.Project;
import com.company.taskmanager.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // Creare proiect nou
    // URL: POST http://localhost:8080/api/projects
    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestBody ProjectRequest request,
            // Principal injecteaza automat user-ul logat curent,din JWT
            Principal principal
    ) {
        String username = principal.getName(); // extragem username-ul din token
        return ResponseEntity.ok(projectService.createProject(request, username));
    }

    // Listare toate proiectele active
    // URL: GET http://localhost:8080/api/projects
    @GetMapping
    public ResponseEntity<List<Project>> getAllActiveProjects() {
        return ResponseEntity.ok(projectService.getAllActiveProjects());
    }

    // Listare proiecte in care user-ul curent este membru
    // URL: GET http://localhost:8080/api/projects/my-projects
    @GetMapping("/my-projects")
    public ResponseEntity<List<Project>> getMyProjects(Principal principal) {
        return ResponseEntity.ok(projectService.getProjectsForMember(principal.getName()));
    }

    // Adaugare membru intr-un proiect existent
    // URL: POST http://localhost:8080/api/projects/1/members?username=nume_utilizator
    @PostMapping("/{id}/members")
    public ResponseEntity<Project> addMemberToProject(
            @PathVariable Long id, // preluam ID-ul proiectului din URL
            @RequestParam String username // preia username-ul din parametrii URL-ului
    ) {
        return ResponseEntity.ok(projectService.addMemberToProject(id, username));
    }

    // Modificare informatii proiect
    // URL: PUT http://localhost:8080/api/projects/1
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    // Stergere proiect, SOFT DELETE
    // URL: DELETE http://localhost:8080/api/projects/1
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        projectService.softDeleteProject(id);
        return ResponseEntity.ok("Proiectul a fost sters cu succes (Soft Delete)!");
    }
}