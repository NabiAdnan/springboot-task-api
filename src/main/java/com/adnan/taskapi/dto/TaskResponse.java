package com.adnan.taskapi.dto;

import com.adnan.taskapi.model.TaskPriority;
import com.adnan.taskapi.model.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id, String title, String description, TaskStatus status, TaskPriority priority,
        LocalDate dueDate, Long projectId, String projectName, String owner,
        Instant createdAt, Instant updatedAt) {}
