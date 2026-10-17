package com.adnan.taskapi.service;

import com.adnan.taskapi.dto.TaskRequest;
import com.adnan.taskapi.dto.TaskResponse;
import com.adnan.taskapi.exception.ResourceNotFoundException;
import com.adnan.taskapi.model.*;
import com.adnan.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private TaskService taskService;

    private User owner() {
        User user = new User("alice", "alice@example.com", "encoded", Role.USER);
        user.setId(1L);
        return user;
    }

    @Test
    void createTask_defaultsToTodoAndMedium() {
        User owner = owner();
        Task saved = new Task();
        saved.setId(10L);
        saved.setTitle("Write tests");
        saved.setStatus(TaskStatus.TODO);
        saved.setPriority(TaskPriority.MEDIUM);
        saved.setOwner(owner);
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskResponse response = taskService.create(
                new TaskRequest("Write tests", "Cover service layer", null, null, null, null), owner);

        assertEquals(10L, response.id());
        assertEquals(TaskStatus.TODO, response.status());
        assertEquals(TaskPriority.MEDIUM, response.priority());
        assertEquals("alice", response.owner());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_withProjectId_resolvesOwnedProject() {
        User owner = owner();
        Project project = new Project("Alpha", "First project", owner);
        project.setId(5L);
        when(projectService.findOwned(5L, owner)).thenReturn(project);
        Task saved = new Task();
        saved.setId(11L);
        saved.setTitle("Scoped task");
        saved.setStatus(TaskStatus.TODO);
        saved.setPriority(TaskPriority.HIGH);
        saved.setProject(project);
        saved.setOwner(owner);
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskResponse response = taskService.create(
                new TaskRequest("Scoped task", null, TaskStatus.TODO, TaskPriority.HIGH, null, 5L), owner);

        assertEquals(5L, response.projectId());
        assertEquals("Alpha", response.projectName());
    }

    @Test
    void getMissingTask_throwsNotFound() {
        User owner = owner();
        when(taskRepository.findByIdAndOwner(99L, owner)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.get(99L, owner));
    }

    @Test
    void deleteTask_deletesOwnedTask() {
        User owner = owner();
        Task task = new Task();
        task.setId(7L);
        task.setOwner(owner);
        when(taskRepository.findByIdAndOwner(7L, owner)).thenReturn(Optional.of(task));

        taskService.delete(7L, owner);

        verify(taskRepository).delete(task);
    }
}
