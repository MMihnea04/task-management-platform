package com.company.taskmanager.controller;

import com.company.taskmanager.dto.TaskRequest;
import com.company.taskmanager.entity.Task;
import com.company.taskmanager.entity.TaskStatus;
import com.company.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // Creare task nou (permis: admin sau membrii proiect)
    // URL complet: POST http://localhost:8080/api/tasks
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForProject(#request.projectId, authentication.name)")
    public ResponseEntity<Task> createTask(
            @RequestBody TaskRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(taskService.createTask(request, principal.getName()));
    }

    // Afisare task-uri dintr-un proiect (permis: admin sau membrii proiect)
    // URL complet: GET http://localhost:8080/api/tasks/project/1
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForProject(#projectId, authentication.name)")
    public ResponseEntity<List<Task>> getTasksForProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksForProject(projectId));
    }

    // Update status la task
    // URL complet: PATCH http://localhost:8080/api/tasks/1/status?status=IN_PROGRESS
    @PatchMapping("/{taskId}/status")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForTask(#taskId, authentication.name)")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam TaskStatus status
    ) {
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, status));
    }

    // Asignare task catre un membru al echipei
    // URL complet: POST http://localhost:8080/api/tasks/1/assign?username=nume_utilizator
    @PostMapping("/{taskId}/assign")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForTask(#taskId, authentication.name)")
    public ResponseEntity<Task> assignTask(
            @PathVariable Long taskId,
            @RequestParam String username
    ) {
        return ResponseEntity.ok(taskService.assignTask(taskId, username));
    }
}