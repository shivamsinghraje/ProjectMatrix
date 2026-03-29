package com.projectmatrix.repository;

import com.projectmatrix.entity.Project;
import com.projectmatrix.entity.Task;
import com.projectmatrix.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByProject(Project project, Pageable pageable);
    Page<Task> findByAssignedTo(User user, Pageable pageable);
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssignedToId(Long userId);
    List<Task> findByStatus(String status);
}