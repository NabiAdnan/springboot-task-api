package com.adnan.taskapi.dto;

import com.adnan.taskapi.model.TaskPriority;
import com.adnan.taskapi.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long projectId) {}
