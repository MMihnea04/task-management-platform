package com.company.taskmanager.controller;

import com.company.taskmanager.dto.ProjectRequest;
import com.company.taskmanager.entity.Project;
import com.company.taskmanager.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // Creare proiect nou (oricine e logat poate crea, devine automat Owner)
    // URL complet: POST http://localhost:8080/api/projects
    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestBody ProjectRequest request,
            Principal principal
    ) {
        String username = principal.getName();
        return ResponseEntity.ok(projectService.createProject(request, username));
    }

    // Listare toate proiectele (doar admin-ul le vede pe toate)
    // URL complet: GET http://localhost:8080/api/projects
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Project>> getAllActiveProjects() {
        return ResponseEntity.ok(projectService.getAllActiveProjects());
    }

    // Listare proiectele mele (oricine e logat isi vede propriile proiecte)
    // URL complet: GET http://localhost:8080/api/projects/my-projects
    @GetMapping("/my-projects")
    public ResponseEntity<List<Project>> getMyProjects(Principal principal) {
        return ResponseEntity.ok(projectService.getProjectsForMember(principal.getName()));
    }

    // Adaugare membru
    // URL complet: POST http://localhost:8080/api/projects/1/members?username=nume_utilizator
    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN') or @projectService.isProjectOwner(#id, authentication.name)")
    public ResponseEntity<Project> addMemberToProject(
            @PathVariable Long id,
            @RequestParam String username
    ) {
        return ResponseEntity.ok(projectService.addMemberToProject(id, username));
    }

    // Modificare date proiect (doar admin sau owner)
    // URL complet: PUT http://localhost:8080/api/projects/1
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @projectService.isProjectOwner(#id, authentication.name)")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    // Stergere proiect (doar admin sau owner)
    // URL complet: DELETE http://localhost:8080/api/projects/1
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @projectService.isProjectOwner(#id, authentication.name)")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        projectService.softDeleteProject(id);
        return ResponseEntity.ok("Proiectul a fost sters cu succes (Soft Delete)!");
    }
}