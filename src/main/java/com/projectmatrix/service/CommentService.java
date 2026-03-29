package com.projectmatrix.service;

import com.projectmatrix.dto.CommentDTO;
import com.projectmatrix.entity.Comment;
import com.projectmatrix.entity.Task;
import com.projectmatrix.exception.CustomException;
import com.projectmatrix.repository.CommentRepository;
import com.projectmatrix.repository.TaskRepository;
import com.projectmatrix.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final MapperUtil mapperUtil;
    private final ActivityService activityService;

    public CommentDTO createComment(CommentDTO commentDTO) {
        Task task = taskRepository.findById(commentDTO.getTaskId())
                .orElseThrow(() -> new CustomException("Task not found"));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setTask(task);
        comment.setUser(userService.getCurrentUser());

        Comment savedComment = commentRepository.save(comment);

        activityService.logActivity("COMMENT_ADDED", "Comment", savedComment.getId(),
                "Comment added to task: " + task.getTitle(), userService.getCurrentUser());

        return mapperUtil.toCommentDTO(savedComment);
    }

    public CommentDTO updateComment(Long id, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Comment not found"));

        comment.setContent(commentDTO.getContent());
        Comment updatedComment = commentRepository.save(comment);

        return mapperUtil.toCommentDTO(updatedComment);
    }

    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Comment not found"));

        commentRepository.delete(comment);

        activityService.logActivity("COMMENT_DELETED", "Comment", id,
                "Comment deleted", userService.getCurrentUser());
    }

    public List<CommentDTO> getCommentsByTask(Long taskId) {
        return commentRepository.findByTaskId(taskId).stream()
                .map(mapperUtil::toCommentDTO)
                .collect(Collectors.toList());
    }
}