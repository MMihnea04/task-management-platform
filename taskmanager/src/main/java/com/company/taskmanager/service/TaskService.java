package com.company.taskmanager.service;

import com.company.taskmanager.dto.TaskRequest;
import com.company.taskmanager.entity.Project;
import com.company.taskmanager.entity.SubTask;
import com.company.taskmanager.entity.Task;
import com.company.taskmanager.entity.TaskStatus;
import com.company.taskmanager.entity.User;
import com.company.taskmanager.repository.ProjectRepository;
import com.company.taskmanager.repository.SubTaskRepository;
import com.company.taskmanager.repository.TaskRepository;
import com.company.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SubTaskRepository subTaskRepository;

    // Verifica daca un utilizator are acces la un proiect (admin, owner sau membru)
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public boolean isUserAuthorizedForTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;

        // Refolosim logica de mai sus pe proiectul de care apartine task-ul
        return isUserAuthorizedForProject(task.getProject().getId(), username);
    }

    @Transactional
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
                .deadline(request.getDeadline())
                .build();

        Task savedTask = taskRepository.save(task);

        // Fortam Hibernate sa aduca datele Lazy pt JSON response
        initializeTaskRelations(savedTask);

        log.info("TASK CREAT: Userul '{}' a creat task-ul '{}' (ID: {}) in proiectul cu ID-ul {}",
                creatorUsername, savedTask.getTitle(), savedTask.getId(), project.getId());

        return savedTask;
    }

    // Editare generala task (Titlu, Descriere)
    @Transactional
    public Task updateTaskDetails(Long taskId, String title, String description, LocalDateTime deadline) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Task-ul nu a fost gasit!"));

        if (title != null && !title.isBlank()) {
            task.setTitle(title);
        }
        if (description != null && !description.isBlank()) {
            task.setDescription(description);
        }
        if (deadline != null) {
            task.setDeadline(deadline);
        }

        Task savedTask = taskRepository.save(task);
        initializeTaskRelations(savedTask);

        return savedTask;
    }

    // Filtrare task-uri dintr-un proiect dupa status si prioritate (optional)
    @Transactional(readOnly = true)
    public List<Task> getTasksForProjectWithFilters(Long projectId, TaskStatus status, String priority) {
        // Luam toate task-urile proiectului
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        // Filtram prin Stream API daca s-au trimis parametrii
        if (status != null) {
            tasks = tasks.stream()
                    .filter(t -> t.getStatus() == status)
                    .toList();
        }
        if (priority != null && !priority.isBlank()) {
            tasks = tasks.stream()
                    .filter(t -> priority.equals(t.getPriority()))
                    .toList();
        }

        // Initialize lazy relations for the whole list
        tasks.forEach(this::initializeTaskRelations);

        return tasks;
    }

    // Mutare task dintr-un status in altul
    @Transactional
    public Task updateTaskStatus(Long taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Task-ul nu exista!"));

        task.setStatus(newStatus);

        task.setStatusChangedAt(LocalDateTime.now()); // Resetam timpul de cand task-ul se afla in noul status
        task.setNeedsAttention(false);               // Daca task-ul s-a miscat, inseamna ca nu mai e blocat

        Task updatedTask = taskRepository.save(task);
        initializeTaskRelations(updatedTask);

        log.info("STATUS TASK UPDATE: Task-ul cu ID-ul {} a fost mutat in statusul {}", taskId, newStatus);

        return updatedTask;
    }

    @Transactional
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
        initializeTaskRelations(updatedTask);

        log.info("PRIORITY UPDATE: Task-ul cu ID-ul {} a received prioritatea {}", taskId, upperPriority);

        return updatedTask;
    }

    // Asign task catre un utilizator
    @Transactional
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
        initializeTaskRelations(assignedTask);

        log.info("TASK ASIGNAT: Task-ul cu ID-ul {} a fost asignat catre utilizatorul '{}'", taskId, assigneeUsername);

        return assignedTask;
    }

    @Transactional
    public Task addSubTask(Long taskId, String title) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Task-ul parinte nu exista!"));

        SubTask subTask = SubTask.builder()
                .title(title)
                .completed(false)
                .task(task)
                .build();

        subTaskRepository.save(subTask);

        // Fortam reincarcarea listei pt a include subtask-ul nou
        task.getSubTasks().add(subTask);
        recalculateAndSaveProgress(task);

        initializeTaskRelations(task);

        log.info("SUBTASK ADAUGAT: Subtask-ul '{}' a fost adaugat la task-id {}", title, taskId);
        return task;
    }

    @Transactional
    public Task toggleSubTask(Long subTaskId) {
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Subtask-ul nu exista!"));

        // Inversam starea booleana, din bifat in nebifat sau viceversa
        subTask.setCompleted(!subTask.isCompleted());
        subTaskRepository.save(subTask);

        Task task = subTask.getTask();
        recalculateAndSaveProgress(task);

        initializeTaskRelations(task);

        log.info("SUBTASK TOGGLE: Subtask-ul cu ID {} are acum starea completed = {}", subTaskId, subTask.isCompleted());
        return task;
    }

    @Transactional
    public Task deleteSubTask(Long taskId, Long subTaskId) {
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Subtask-ul nu exista!"));

        subTaskRepository.delete(subTask);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Task-ul nu exista!"));

        task.getSubTasks().remove(subTask);
        recalculateAndSaveProgress(task);

        initializeTaskRelations(task);

        log.info("SUBTASK STERS: Subtask-ul cu ID {} a fost sters din task-id {}", subTaskId, taskId);
        return task;
    }

    // Stergere fizica Task
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Task-ul nu a fost gasit!"));

        taskRepository.delete(task);
        log.info("TASK STERS: Task-ul cu ID-ul {} a fost sters din sistem.", taskId);
    }

    private void recalculateAndSaveProgress(Task task) {
        List<SubTask> subTasks = task.getSubTasks();

        if (subTasks == null || subTasks.isEmpty()) {
            task.setProgress(0);
        } else {
            long completedCount = subTasks.stream()
                    .filter(SubTask::isCompleted)
                    .count();

            // Formula: (Bifate / Total) * 100
            int newProgress = (int) ((completedCount * 100) / subTasks.size());
            task.setProgress(newProgress);
        }

        // Salvam modificarea progresului în baza de date
        taskRepository.save(task);
    }

    @Transactional
    public Task autoRouteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Eroare: Task-ul nu exista!"));

        Project project = task.getProject();

        java.util.List<User> candidates = new java.util.ArrayList<>();
        candidates.add(project.getOwner());
        if (project.getMembers() != null) {
            candidates.addAll(project.getMembers());
        }

        java.util.List<TaskStatus> activeStatuses = java.util.List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS);

        User optimalUser = null;
        long minWorkload = Long.MAX_VALUE;

        for (User candidate : candidates) {
            long currentWorkload = taskRepository.countByAssigneeAndStatusIn(candidate, activeStatuses);

            if (currentWorkload < minWorkload) {
                minWorkload = currentWorkload;
                optimalUser = candidate;
            }
            else if (currentWorkload == minWorkload && candidate.equals(project.getOwner())) {
                optimalUser = candidate;
            }
        }

        if (optimalUser == null) {
            throw new RuntimeException("Eroare: Nu s-au gasit utilizatori eligibili pentru acest proiect!");
        }

        task.setAssignee(optimalUser);
        Task routedTask = taskRepository.save(task);
        initializeTaskRelations(routedTask);

        log.info("SMART ROUTER: Task-ul cu ID {} a fost asignat automat catre '{}' (Workload activ: {} task-uri)",
                taskId, optimalUser.getUsername(), minWorkload);

        return routedTask;
    }

    // Metoda ajutatoare pentru a forta Hibernate sa citeasca datele inainte de a le trimite catre JSON
    private void initializeTaskRelations(Task task) {
        if (task.getCreator() != null) task.getCreator().getRoles().size();
        if (task.getAssignee() != null) task.getAssignee().getRoles().size();
        if (task.getSubTasks() != null) task.getSubTasks().size();
        if (task.getProject() != null) {
            task.getProject().getOwner().getRoles().size();
            if (task.getProject().getMembers() != null) {
                task.getProject().getMembers().forEach(m -> m.getRoles().size());
            }
        }
    }
}