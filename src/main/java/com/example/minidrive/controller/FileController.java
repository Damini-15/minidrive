package com.example.minidrive.controller;

import com.example.minidrive.entity.File;
import com.example.minidrive.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<File> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            File uploadedFile = fileService.uploadFile(file, email);

            return ResponseEntity.ok(uploadedFile);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}