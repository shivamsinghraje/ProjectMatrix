package com.projectmatrix.repository;

import com.projectmatrix.entity.ActivityLog;
import com.projectmatrix.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<ActivityLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
}