package com.projectmatrix.service;

import com.projectmatrix.entity.ActivityLog;
import com.projectmatrix.entity.User;
import com.projectmatrix.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;

    public void logActivity(String action, String entityType, Long entityId, String details, User user) {
        ActivityLog log = new ActivityLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setUser(user);

        activityRepository.save(log);
    }

    public Page<ActivityLog> getUserActivities(User user, Pageable pageable) {
        return activityRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
}