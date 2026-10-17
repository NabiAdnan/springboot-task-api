package com.adnan.taskapi.repository;

import com.adnan.taskapi.model.Task;
import com.adnan.taskapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Optional<Task> findByIdAndOwner(Long id, User owner);
}
