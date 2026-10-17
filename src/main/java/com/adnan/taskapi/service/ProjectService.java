package com.adnan.taskapi.service;

import com.adnan.taskapi.dto.ProjectRequest;
import com.adnan.taskapi.dto.ProjectResponse;
import com.adnan.taskapi.exception.ResourceNotFoundException;
import com.adnan.taskapi.model.Project;
import com.adnan.taskapi.model.User;
import com.adnan.taskapi.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public static ProjectResponse toResponse(Project p) {
        return new ProjectResponse(p.getId(), p.getName(), p.getDescription(),
                p.getOwner().getUsername(), p.getCreatedAt());
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request, User owner) {
        Project project = new Project(request.name(), request.description(), owner);
        return toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(User owner) {
        return projectRepository.findByOwner(owner).stream().map(ProjectService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long id, User owner) {
        return toResponse(findOwned(id, owner));
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request, User owner) {
        Project project = findOwned(id, owner);
        project.setName(request.name());
        project.setDescription(request.description());
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id, User owner) {
        projectRepository.delete(findOwned(id, owner));
    }

    public Project findOwned(Long id, User owner) {
        return projectRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }
}
