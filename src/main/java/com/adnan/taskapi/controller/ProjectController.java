package com.adnan.taskapi.controller;

import com.adnan.taskapi.dto.ProjectRequest;
import com.adnan.taskapi.dto.ProjectResponse;
import com.adnan.taskapi.model.User;
import com.adnan.taskapi.service.ProjectService;
import com.adnan.taskapi.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserDetailsServiceImpl userDetailsService;

    public ProjectController(ProjectService projectService, UserDetailsServiceImpl userDetailsService) {
        this.projectService = projectService;
        this.userDetailsService = userDetailsService;
    }

    private User currentUser(UserDetails principal) {
        return userDetailsService.loadDomainUser(principal.getUsername());
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request,
                                                  @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request, currentUser(principal)));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(projectService.list(currentUser(principal)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> get(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(projectService.get(id, currentUser(principal)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ProjectRequest request,
                                                  @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(projectService.update(id, request, currentUser(principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        projectService.delete(id, currentUser(principal));
        return ResponseEntity.noContent().build();
    }
}
