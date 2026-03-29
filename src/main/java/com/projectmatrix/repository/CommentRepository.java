package com.projectmatrix.repository;

import com.projectmatrix.entity.Comment;
import com.projectmatrix.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskOrderByCreatedAtDesc(Task task);
    List<Comment> findByTaskId(Long taskId);
}