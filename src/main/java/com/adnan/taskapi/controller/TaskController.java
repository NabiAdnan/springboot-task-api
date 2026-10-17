package com.adnan.taskapi.controller;

import com.adnan.taskapi.dto.TaskRequest;
import com.adnan.taskapi.dto.TaskResponse;
import com.adnan.taskapi.model.TaskPriority;
import com.adnan.taskapi.model.TaskStatus;
import com.adnan.taskapi.model.User;
import com.adnan.taskapi.service.TaskService;
import com.adnan.taskapi.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserDetailsServiceImpl userDetailsService;

    public TaskController(TaskService taskService, UserDetailsServiceImpl userDetailsService) {
        this.taskService = taskService;
        this.userDetailsService = userDetailsService;
    }

    private User currentUser(UserDetails principal) {
        return userDetailsService.loadDomainUser(principal.getUsername());
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request,
                                               @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request, currentUser(principal)));
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> list(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(taskService.list(currentUser(principal), status, priority, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> get(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(taskService.get(id, currentUser(principal)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody TaskRequest request,
                                               @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(taskService.update(id, request, currentUser(principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        taskService.delete(id, currentUser(principal));
        return ResponseEntity.noContent().build();
    }
}
