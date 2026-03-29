package com.projectmatrix.service;

import com.projectmatrix.dto.FileDTO;
import com.projectmatrix.entity.FileAttachment;
import com.projectmatrix.entity.Task;
import com.projectmatrix.exception.CustomException;
import com.projectmatrix.repository.FileRepository;
import com.projectmatrix.repository.TaskRepository;
import com.projectmatrix.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FileService {

    private final FileRepository fileRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final MapperUtil mapperUtil;
    private final ActivityService activityService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public FileDTO uploadFile(Long taskId, MultipartFile file) throws IOException {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException("Task not found"));

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setFileName(file.getOriginalFilename());
        fileAttachment.setFilePath(filePath.toString());
        fileAttachment.setFileType(file.getContentType());
        fileAttachment.setFileSize(file.getSize());
        fileAttachment.setTask(task);
        fileAttachment.setUploadedBy(userService.getCurrentUser());

        FileAttachment savedFile = fileRepository.save(fileAttachment);

        activityService.logActivity("FILE_UPLOADED", "File", savedFile.getId(),
                "File uploaded to task: " + task.getTitle(), userService.getCurrentUser());

        return mapperUtil.toFileDTO(savedFile);
    }

    public void deleteFile(Long id) throws IOException {
        FileAttachment file = fileRepository.findById(id)
                .orElseThrow(() -> new CustomException("File not found"));

        Path filePath = Paths.get(file.getFilePath());
        Files.deleteIfExists(filePath);

        fileRepository.delete(file);

        activityService.logActivity("FILE_DELETED", "File", id,
                "File deleted: " + file.getFileName(), userService.getCurrentUser());
    }

    public List<FileDTO> getFilesByTask(Long taskId) {
        return fileRepository.findByTaskId(taskId).stream()
                .map(mapperUtil::toFileDTO)
                .collect(Collectors.toList());
    }

    public FileDTO getFileById(Long id) {
        FileAttachment file = fileRepository.findById(id)
                .orElseThrow(() -> new CustomException("File not found"));
        return mapperUtil.toFileDTO(file);
    }
}