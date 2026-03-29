package com.projectmatrix.repository;

import com.projectmatrix.entity.FileAttachment;
import com.projectmatrix.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileAttachment, Long> {
    List<FileAttachment> findByTask(Task task);
    List<FileAttachment> findByTaskId(Long taskId);
}