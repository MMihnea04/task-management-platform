package com.company.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskRequest {
    @NotBlank(message = "Titlul task-ului este obligatoriu!")
    private String title;

    private String description;

    @NotNull(message = "ID-ul proiectului este obligatoriu!")
    private Long projectId;

    private LocalDateTime deadline;
}