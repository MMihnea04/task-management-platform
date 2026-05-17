package com.company.taskmanager.service;

import com.company.taskmanager.dto.TaskRequest;
import com.company.taskmanager.entity.Project;
import com.company.taskmanager.entity.Task;
import com.company.taskmanager.entity.TaskStatus;
import com.company.taskmanager.entity.User;
import com.company.taskmanager.repository.ProjectRepository;
import com.company.taskmanager.repository.TaskRepository;
import com.company.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // Verifica daca un utilizator are acces la un proiect (admin, owner sau membru)
    public boolean isUserAuthorizedForProject(Long projectId, String username) {
        // cautam proiectul
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return false;

        if (project.isDeleted()) return false;

        // Verificam daca user-ul este owner-ul sau daca este in lista de membri
        boolean isOwner = project.getOwner().getUsername().equals(username);
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUsername().equals(username));

        return isOwner || isMember;
    }

    // Verifica daca userul are acces direct la un Task existent (pentru modificare/asignare)
    public boolean isUserAuthorizedForTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;

        // Refolosim logica de mai sus pe proiectul de care apartine task-ul
        return isUserAuthorizedForProject(task.getProject().getId(), username);
    }

    public Task createTask(TaskRequest request, String creatorUsername) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Eroare: Proiectul nu exista!"));

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul nu exista!"));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO) // Orice task nou intra cu status TODO
                .project(project)
                .creator(creator)
                .build();

        Task savedTask = taskRepository.save(task);

        log.info("TASK CREAT: Userul '{}' a creat task-ul '{}' (ID: {}) in proiectul cu ID-ul {}",
                creatorUsername, savedTask.getTitle(), savedTask.getId(), project.getId());

        return savedTask;
    }

    // Arata task-uri per proiect
    public List<Task> getTasksForProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    // Mutare task dintr-un status in altul
    public Task updateTaskStatus(Long taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Task-ul nu exista!"));

        task.setStatus(newStatus);

        task.setStatusChangedAt(LocalDateTime.now()); // Resetam timpul de cand task-ul se afla in noul status
        task.setNeedsAttention(false);               // Daca task-ul s-a miscat, inseamna ca nu mai e blocat

        Task updatedTask = taskRepository.save(task);

        log.info("STATUS TASK UPDATE: Task-ul cu ID-ul {} a fost mutat in statusul {}", taskId, newStatus);

        return updatedTask;
    }

    public Task updateTaskPriority(Long taskId, String priority) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Task-ul nu exista!"));

        String upperPriority = priority.toUpperCase();

        // Validare simpla ca sa poata scrie doar prioritati
        if (!java.util.List.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(upperPriority)) {
            throw new RuntimeException("Eroare: Prioritate invalida! Foloseste LOW, MEDIUM, HIGH sau CRITICAL.");
        }

        task.setPriority(upperPriority);

        // Resetam ceasul pt ca noul status de prioritate să fie monitorizat curat de acum încolo
        task.setStatusChangedAt(java.time.LocalDateTime.now());
        task.setNeedsAttention(false);

        Task updatedTask = taskRepository.save(task);

        log.info("PRIORITY UPDATE: Task-ul cu ID-ul {} a primit prioritatea {}", taskId, upperPriority);

        return updatedTask;
    }

    // Asign task catre un utilizator
    public Task assignTask(Long taskId, String assigneeUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Task-ul nu a fost gasit!"));

        User assignee = userRepository.findByUsername(assigneeUsername)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul de asignat nu exista!"));

        // Verificam daca user-ul face parte din proiect inainte sa ii dam task-ul
        if (!task.getProject().getMembers().contains(assignee) && !task.getProject().getOwner().equals(assignee)) {
            throw new RuntimeException("Eroare: Utilizatorul nu este membru al acestui proiect!");
        }

        task.setAssignee(assignee);

        Task assignedTask = taskRepository.save(task);

        log.info("TASK ASIGNAT: Task-ul cu ID-ul {} a fost asignat catre utilizatorul '{}'", taskId, assigneeUsername);

        return assignedTask;
    }
}