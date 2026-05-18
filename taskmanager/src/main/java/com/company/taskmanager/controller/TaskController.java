package com.company.taskmanager.controller;

import com.company.taskmanager.dto.TaskRequest;
import com.company.taskmanager.entity.Task;
import com.company.taskmanager.entity.TaskStatus;
import com.company.taskmanager.service.TaskService;
import jakarta.validation.Valid;
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
    @PreAuthorize("hasRole('ADMIN') or @taskService.isUserAuthorizedForProject(#request.projectId, authentication.name)")
    public ResponseEntity<Task> createTask(
            @Valid @RequestBody TaskRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(taskService.createTask(request, principal.getName()));
    }

    // Editare generala task (Titlu, Descriere, Deadline)
    // URL complet: PUT http://localhost:8080/api/tasks/1
    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or @taskService.isUserAuthorizedForTask(#taskId, authentication.name)")
    public ResponseEntity<Task> updateTaskDetails(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTaskDetails(taskId, request.getTitle(), request.getDescription(), request.getDeadline()));
    }

    // Afisare task-uri dintr-un proiect CU filtrare optionala
    // URL complet: GET http://localhost:8080/api/tasks/project/1?status=TODO&priority=HIGH
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForProject(#projectId, authentication.name)")
    public ResponseEntity<List<Task>> getTasksForProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String priority
    ) {
        return ResponseEntity.ok(taskService.getTasksForProjectWithFilters(projectId, status, priority));
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
    @PatchMapping("/{taskId}/assign")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForTask(#taskId, authentication.name)")
    public ResponseEntity<Task> assignTask(
            @PathVariable Long taskId,
            @RequestParam String username
    ) {
        return ResponseEntity.ok(taskService.assignTask(taskId, username));
    }

    // Update prioritate la task
    // URL complet: PATCH http://localhost:8080/api/tasks/1/priority?priority=CRITICAL
    @PatchMapping("/{taskId}/priority")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForTask(#taskId, authentication.name)")
    public ResponseEntity<Task> updateTaskPriority(
            @PathVariable Long taskId,
            @RequestParam String priority
    ) {
        return ResponseEntity.ok(taskService.updateTaskPriority(taskId, priority));
    }

    // Adaugare subtask nou intr-un task
    // URL complet: POST http://localhost:8080/api/tasks/1/subtasks?title=nume_subtask
    @PostMapping("/{taskId}/subtasks")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForTask(#taskId, authentication.name)")
    public ResponseEntity<Task> addSubTask(
            @PathVariable Long taskId,
            @RequestParam String title
    ) {
        return ResponseEntity.ok(taskService.addSubTask(taskId, title));
    }

    // Bifare / Debifare subtask
    // URL complet: PATCH http://localhost:8080/api/tasks/1/subtasks/5/toggle
    @PatchMapping("/{taskId}/subtasks/{subTaskId}/toggle")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForTask(#taskId, authentication.name)")
    public ResponseEntity<Task> toggleSubTask(
            @PathVariable Long taskId,
            @PathVariable Long subTaskId
    ) {
        return ResponseEntity.ok(taskService.toggleSubTask(subTaskId));
    }

    // Stergere subtask dintr-un task
    // URL complet: DELETE http://localhost:8080/api/tasks/1/subtasks/5
    @DeleteMapping("/{taskId}/subtasks/{subTaskId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForTask(#taskId, authentication.name)")
    public ResponseEntity<Task> deleteSubTask(
            @PathVariable Long taskId,
            @PathVariable Long subTaskId
    ) {
        return ResponseEntity.ok(taskService.deleteSubTask(taskId, subTaskId));
    }

    // Stergere task
    // URL complet: DELETE http://localhost:8080/api/tasks/1
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or @taskService.isUserAuthorizedForTask(#taskId, authentication.name)")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok("Task-ul a fost sters cu succes!");
    }

    // Asignare automata inteligenta in functie de workload-ul echipei
    // URL complet: POST http://localhost:8080/api/tasks/1/auto-route
    @PostMapping("/{taskId}/auto-route")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@taskService.isUserAuthorizedForTask(#taskId, authentication.name)")
    public ResponseEntity<Task> autoRouteTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.autoRouteTask(taskId));
    }
}