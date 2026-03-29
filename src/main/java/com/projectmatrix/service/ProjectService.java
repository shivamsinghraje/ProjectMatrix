package com.projectmatrix.service;

import com.projectmatrix.dto.ProjectDTO;
import com.projectmatrix.entity.Project;
import com.projectmatrix.entity.Role;
import com.projectmatrix.entity.User;
import com.projectmatrix.exception.CustomException;
import com.projectmatrix.repository.ProjectRepository;
import com.projectmatrix.repository.UserRepository;
import com.projectmatrix.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MapperUtil mapperUtil;
    private final ActivityService activityService;

    public ProjectDTO createProject(ProjectDTO projectDTO) {
        log.debug("Creating project with data: {}", projectDTO);

        try {
            // Get current user
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            log.debug("Current user email: {}", currentUserEmail);

            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new CustomException("Current user not found: " + currentUserEmail));
            log.debug("Current user found: {} {}", currentUser.getFirstName(), currentUser.getLastName());

            // Create new project
            Project project = new Project();
            project.setName(projectDTO.getName());
            project.setDescription(projectDTO.getDescription());
            project.setStatus("ACTIVE");
            project.setStartDate(projectDTO.getStartDate());
            project.setEndDate(projectDTO.getEndDate());
            project.setCreatedBy(currentUser);

            // Initialize users set with creator
            Set<User> users = new HashSet<>();
            users.add(currentUser);

            // Add selected users if any
            if (projectDTO.getUserIds() != null && !projectDTO.getUserIds().isEmpty()) {
                log.debug("Adding users with IDs: {}", projectDTO.getUserIds());
                List<User> selectedUsers = userRepository.findAllById(projectDTO.getUserIds());
                users.addAll(selectedUsers);
            }

            project.setUsers(users);

            log.debug("Saving project...");
            Project savedProject = projectRepository.save(project);
            log.debug("Project saved with ID: {}", savedProject.getId());

            // Log activity
            activityService.logActivity("PROJECT_CREATED", "Project", savedProject.getId(),
                    "Project created: " + savedProject.getName(), currentUser);

            ProjectDTO result = mapperUtil.toProjectDTO(savedProject);
            log.debug("Returning project DTO: {}", result);

            return result;

        } catch (Exception e) {
            log.error("Error creating project: ", e);
            throw new CustomException("Error creating project: " + e.getMessage());
        }
    }

    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        log.debug("Updating project {} with data: {}", id, projectDTO);

        try {
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new CustomException("Project not found"));

            // Update basic fields
            project.setName(projectDTO.getName());
            project.setDescription(projectDTO.getDescription());
            project.setStartDate(projectDTO.getStartDate());
            project.setEndDate(projectDTO.getEndDate());

            // Keep existing status if not provided
            if (projectDTO.getStatus() != null) {
                project.setStatus(projectDTO.getStatus());
            }


            // Update users if provided
            if (projectDTO.getUserIds() != null && !projectDTO.getUserIds().isEmpty()) {
                Set<User> users = new HashSet<>(userRepository.findAllById(projectDTO.getUserIds()));

                // Make sure the creator is still in the users list
                User creator = project.getCreatedBy();
                users.add(creator);

                project.setUsers(users);
            }

            log.debug("Saving updated project...");
            Project updatedProject = projectRepository.save(project);
            log.debug("Project updated with ID: {}", updatedProject.getId());

            activityService.logActivity("PROJECT_UPDATED", "Project", updatedProject.getId(),
                    "Project updated: " + updatedProject.getName(), getCurrentUser());

            ProjectDTO result = mapperUtil.toProjectDTO(updatedProject);
            log.debug("Returning updated DTO: {}", result);

            return result;

        } catch (Exception e) {
            log.error("Error in updateProject: ", e);
            throw new CustomException("Failed to update project: " + e.getMessage());
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Current user not found"));
    }


    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new CustomException("Project not found"));

        projectRepository.delete(project);

        activityService.logActivity("PROJECT_DELETED", "Project", id,
                "Project deleted: " + project.getName(), userService.getCurrentUser());
    }

    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new CustomException("Project not found"));

        ProjectDTO dto = mapperUtil.toProjectDTO(project);
        dto.setUsers(project.getUsers().stream()
                .map(mapperUtil::toUserDTO)
                .collect(Collectors.toSet()));

        return dto;
    }

    public Page<ProjectDTO> getUserProjects(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return projectRepository.findByUsersContaining(currentUser, pageable)
                .map(mapperUtil::toProjectDTO);
    }


    public List<ProjectDTO> getAllProjects() {
        try {
            log.info("Getting all projects");

            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Current user email: {}", currentUserEmail);

            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new CustomException("Current user not found"));
            log.info("Current user: {} with role: {}", currentUser.getEmail(), currentUser.getRole());

            List<Project> projects;

            if (currentUser.getRole() == Role.ADMIN) {
                log.info("User is ADMIN, fetching all projects");
                projects = projectRepository.findAll();
            } else {
                log.info("User is not ADMIN, fetching user's projects");
                projects = projectRepository.findByUserInvolved(currentUser);
            }

            log.info("Found {} projects", projects.size());

            List<ProjectDTO> projectDTOs = projects.stream()
                    .map(mapperUtil::toProjectDTO)
                    .collect(Collectors.toList());

            log.info("Mapped to {} DTOs", projectDTOs.size());

            return projectDTOs;

        } catch (Exception e) {
            log.error("Error in getAllProjects: ", e);
            throw new CustomException("Failed to get projects: " + e.getMessage());
        }
    }

    public ProjectDTO assignUsers(Long projectId, Set<Long> userIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException("Project not found"));

        Set<User> users = new HashSet<>(userRepository.findAllById(userIds));
        project.setUsers(users);
        Project updatedProject = projectRepository.save(project);

        activityService.logActivity("USERS_ASSIGNED", "Project", projectId,
                "Users assigned to project", userService.getCurrentUser());

        return mapperUtil.toProjectDTO(updatedProject);
    }
}