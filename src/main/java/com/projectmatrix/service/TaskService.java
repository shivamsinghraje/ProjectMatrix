package com.projectmatrix.service;

import com.projectmatrix.dto.TaskDTO;
import com.projectmatrix.entity.Project;
import com.projectmatrix.entity.Task;
import com.projectmatrix.entity.User;
import com.projectmatrix.exception.CustomException;
import com.projectmatrix.repository.ProjectRepository;
import com.projectmatrix.repository.TaskRepository;
import com.projectmatrix.repository.UserRepository;
import com.projectmatrix.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MapperUtil mapperUtil;
    private final ActivityService activityService;

    public TaskDTO createTask(TaskDTO taskDTO) {
        Project project = projectRepository.findById(taskDTO.getProjectId())
                .orElseThrow(() -> new CustomException("Project not found"));

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : "TODO");
        task.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : "MEDIUM");
        task.setDueDate(taskDTO.getDueDate());
        task.setProject(project);
        task.setCreatedBy(userService.getCurrentUser());

        if (taskDTO.getAssignedToId() != null) {
            User assignedUser = userRepository.findById(taskDTO.getAssignedToId())
                    .orElseThrow(() -> new CustomException("Assigned user not found"));
            task.setAssignedTo(assignedUser);
        }

        Task savedTask = taskRepository.save(task);

        activityService.logActivity("TASK_CREATED", "Task", savedTask.getId(),
                "Task created: " + savedTask.getTitle(), userService.getCurrentUser());

        return mapperUtil.toTaskDTO(savedTask);
    }

    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new CustomException("Task not found"));

        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus());
        task.setPriority(taskDTO.getPriority());
        task.setDueDate(taskDTO.getDueDate());

        if (taskDTO.getAssignedToId() != null) {
            User assignedUser = userRepository.findById(taskDTO.getAssignedToId())
                    .orElseThrow(() -> new CustomException("Assigned user not found"));
            task.setAssignedTo(assignedUser);
        }

        Task updatedTask = taskRepository.save(task);

        activityService.logActivity("TASK_UPDATED", "Task", updatedTask.getId(),
                "Task updated: " + updatedTask.getTitle(), userService.getCurrentUser());

        return mapperUtil.toTaskDTO(updatedTask);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new CustomException("Task not found"));

        taskRepository.delete(task);

        activityService.logActivity("TASK_DELETED", "Task", id,
                "Task deleted: " + task.getTitle(), userService.getCurrentUser());
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new CustomException("Task not found"));
        return mapperUtil.toTaskDTO(task);
    }

    public List<TaskDTO> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(mapperUtil::toTaskDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByUser(Long userId) {
        return taskRepository.findByAssignedToId(userId).stream()
                .map(mapperUtil::toTaskDTO)
                .collect(Collectors.toList());
    }

    public Page<TaskDTO> getUserTasks(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return taskRepository.findByAssignedTo(currentUser, pageable)
                .map(mapperUtil::toTaskDTO);
    }

    public TaskDTO updateTaskStatus(Long id, String status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new CustomException("Task not found"));

        String oldStatus = task.getStatus();
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        activityService.logActivity("TASK_STATUS_UPDATED", "Task", id,
                "Task status changed from " + oldStatus + " to " + status,
                userService.getCurrentUser());

        return mapperUtil.toTaskDTO(updatedTask);
    }

    public TaskDTO assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));

        task.setAssignedTo(user);
        Task updatedTask = taskRepository.save(task);

        activityService.logActivity("TASK_ASSIGNED", "Task", taskId,
                "Task assigned to " + user.getFirstName() + " " + user.getLastName(),
                userService.getCurrentUser());

        return mapperUtil.toTaskDTO(updatedTask);
    }
}