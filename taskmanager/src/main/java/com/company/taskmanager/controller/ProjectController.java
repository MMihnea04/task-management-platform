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

    // Creare proiect nou (Oricine e logat poate crea, devine automat Owner)
    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestBody ProjectRequest request,
            Principal principal
    ) {
        String username = principal.getName();
        return ResponseEntity.ok(projectService.createProject(request, username));
    }

    // Listare toate proiectele (Doar Adminul le vede pe toate)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Project>> getAllActiveProjects() {
        return ResponseEntity.ok(projectService.getAllActiveProjects());
    }

    // Listare proiectele mele (Oricine e logat isi vede propriile proiecte)
    @GetMapping("/my-projects")
    public ResponseEntity<List<Project>> getMyProjects(Principal principal) {
        return ResponseEntity.ok(projectService.getProjectsForMember(principal.getName()));
    }

    // 🔒 SECURIZAT: Adaugare membru
    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN') or @projectService.isProjectOwner(#id, authentication.name)")
    public ResponseEntity<Project> addMemberToProject(
            @PathVariable Long id,
            @RequestParam String username
    ) {
        return ResponseEntity.ok(projectService.addMemberToProject(id, username));
    }

    // 🔒 SECURIZAT: Modificare date proiect (Doar Admin sau Owner)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @projectService.isProjectOwner(#id, authentication.name)")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    // 🔒 SECURIZAT: Stergere proiect (Doar Admin sau Owner)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @projectService.isProjectOwner(#id, authentication.name)")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        projectService.softDeleteProject(id);
        return ResponseEntity.ok("Proiectul a fost sters cu succes (Soft Delete)!");
    }
}