
package com.projectmatrix.controller;

import com.projectmatrix.dto.ProjectDTO;
import com.projectmatrix.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<?> getAllProjects() {
        log.info("GET /projects called");
        try {
            List<ProjectDTO> projects = projectService.getAllProjects();
            log.info("Returning {} projects", projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            log.error("Error getting projects: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch projects: " + e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Map<String, Object> payload) {
        log.info("=== POST /projects - START ===");
        log.info("Raw payload: {}", payload);

        try {
            ProjectDTO projectDTO = convertPayloadToDTO(payload);
            log.info("Created DTO: {}", projectDTO);

            ProjectDTO created = projectService.createProject(projectDTO);
            log.info("Service returned: {}", created);

            Map<String, Object> response = new HashMap<>();
            response.put("id", created.getId());
            response.put("name", created.getName());
            response.put("message", "Project created successfully");

            log.info("=== POST /projects - SUCCESS ===");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("=== POST /projects - ERROR ===", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        log.info("=== PUT /projects/{} - START ===", id);
        log.info("Raw payload: {}", payload);

        try {
            ProjectDTO projectDTO = convertPayloadToDTO(payload);
            projectDTO.setId(id);
            log.info("Created DTO for update: {}", projectDTO);

            ProjectDTO updated = projectService.updateProject(id, projectDTO);
            log.info("Service returned: {}", updated);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updated.getId());
            response.put("name", updated.getName());
            response.put("message", "Project updated successfully");

            log.info("=== PUT /projects/{} - SUCCESS ===", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("=== PUT /projects/{} - ERROR ===", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Helper method to convert payload to DTO
    private ProjectDTO convertPayloadToDTO(Map<String, Object> payload) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName((String) payload.get("name"));
        projectDTO.setDescription((String) payload.get("description"));

        // Parse dates
        String startDateStr = (String) payload.get("startDate");
        String endDateStr = (String) payload.get("endDate");

        if (startDateStr != null && !startDateStr.isEmpty()) {
            projectDTO.setStartDate(LocalDate.parse(startDateStr));
        }
        if (endDateStr != null && !endDateStr.isEmpty()) {
            projectDTO.setEndDate(LocalDate.parse(endDateStr));
        }

        // Handle userIds
        Object userIdsObj = payload.get("userIds");
        if (userIdsObj != null) {
            Set<Long> userIds = new HashSet<>();
            if (userIdsObj instanceof List) {
                List<?> userIdList = (List<?>) userIdsObj;
                userIds = userIdList.stream()
                        .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
                        .collect(Collectors.toSet());
            }
            projectDTO.setUserIds(userIds);
        }

        return projectDTO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        log.info("GET /projects/{} called", id);
        try {
            ProjectDTO project = projectService.getProjectById(id);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            log.error("Error getting project by id: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/my-projects")
    public ResponseEntity<?> getUserProjects(Pageable pageable) {
        log.info("GET /projects/my-projects called");
        try {
            Page<ProjectDTO> projects = projectService.getUserProjects(pageable);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            log.error("Error getting user projects: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        log.info("DELETE /projects/{} called", id);
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting project: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}/assign-users")
    public ResponseEntity<?> assignUsers(@PathVariable Long id, @RequestBody Set<Long> userIds) {
        log.info("PUT /projects/{}/assign-users called", id);
        try {
            ProjectDTO updated = projectService.assignUsers(id, userIds);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error assigning users: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}