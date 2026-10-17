package com.adnan.taskapi.service;

import com.adnan.taskapi.dto.TaskRequest;
import com.adnan.taskapi.dto.TaskResponse;
import com.adnan.taskapi.exception.ResourceNotFoundException;
import com.adnan.taskapi.model.*;
import com.adnan.taskapi.repository.TaskRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    public TaskService(TaskRepository taskRepository, ProjectService projectService) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
    }

    public static TaskResponse toResponse(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority(),
                t.getDueDate(),
                t.getProject() != null ? t.getProject().getId() : null,
                t.getProject() != null ? t.getProject().getName() : null,
                t.getOwner().getUsername(), t.getCreatedAt(), t.getUpdatedAt());
    }

    @Transactional
    public TaskResponse create(TaskRequest request, User owner) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setDueDate(request.dueDate());
        task.setOwner(owner);
        if (request.projectId() != null) {
            task.setProject(projectService.findOwned(request.projectId(), owner));
        }
        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(User owner, TaskStatus status, TaskPriority priority, String search, Pageable pageable) {
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("owner"), owner));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return taskRepository.findAll(spec, pageable).map(TaskService::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id, User owner) {
        return toResponse(findOwned(id, owner));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request, User owner) {
        Task task = findOwned(id, owner);
        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        task.setDueDate(request.dueDate());
        if (request.projectId() != null) {
            task.setProject(projectService.findOwned(request.projectId(), owner));
        } else {
            task.setProject(null);
        }
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id, User owner) {
        taskRepository.delete(findOwned(id, owner));
    }

    public Task findOwned(Long id, User owner) {
        return taskRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }
}
