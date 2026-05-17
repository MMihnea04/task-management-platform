package com.company.taskmanager.service;

import com.company.taskmanager.entity.Task;
import com.company.taskmanager.entity.TaskStatus;
import com.company.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskAlertScheduler {

    private final TaskRepository taskRepository;

    // Ruleaza la fiecare ora
    // Testing: Inlocuieste cron-ul cu fixedDelay = 10000(10 sec)
    @Scheduled(cron = "0 0 * * * *")
    public void checkStuckCriticalTasks() {
        log.info("SLA CRON: Se porneste scanarea task-urilor pentru verificarea blocajelor...");

        // Calculam pragul de 24 de ore in urma
        // Testing: Inlocuim cu LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);
        // ca sa vedem daca au trecut 2 min in loc de 24 de ore
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);

        // Cautam toate task-urile active din DB
        List<Task> activeTasks = taskRepository.findAll();

        for (Task task : activeTasks) {
            // Verificam daca e CRITICAL, IN_PROGRESS si a depasit 24 de ore fara modificari
            if ("CRITICAL".equalsIgnoreCase(task.getPriority())
                    && task.getStatus() == TaskStatus.IN_PROGRESS
                    && task.getStatusChangedAt().isBefore(threshold)
                    && !task.isNeedsAttention()) {

                // Activam alerta
                task.setNeedsAttention(true);
                taskRepository.save(task);

                log.warn("ATENTIE ECHIPA: Task-ul '{}' (ID: {}) este in lucru de peste 24 de ore. Are nevoie de ajutor din partea echipei?",
                        task.getTitle(), task.getId());
            }
        }
    }
}