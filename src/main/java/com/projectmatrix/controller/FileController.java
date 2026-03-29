package com.projectmatrix.controller;

import com.projectmatrix.dto.FileDTO;
import com.projectmatrix.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@CrossOrigin
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload/{taskId}")
    public ResponseEntity<FileDTO> uploadFile(@PathVariable Long taskId,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.uploadFile(taskId, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) throws IOException {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<FileDTO>> getFilesByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(fileService.getFilesByTask(taskId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDTO> getFileById(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getFileById(id));
    }
}