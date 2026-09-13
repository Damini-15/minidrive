package com.example.minidrive.controller;

import com.example.minidrive.entity.File;
import com.example.minidrive.service.FileService;
import com.example.minidrive.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final S3Service s3Service;
    public FileController(FileService fileService, S3Service s3Service) {
        this.fileService = fileService;
        this.s3Service = s3Service;
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
    @GetMapping
    public ResponseEntity<?> getUserFiles(
            Authentication authentication) {

        try {
            String email = authentication.getName();

            return ResponseEntity.ok(
                    fileService.getUserFiles(email)
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            String email = authentication.getName();

            File file = fileService.getFileForDownload(id, email);

            byte[] data = s3Service.downloadFile(file.getFilePath());

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getFileName() + "\""
                    )
                    .contentType(
                            MediaType.APPLICATION_OCTET_STREAM
                    )
                    .body(data);

        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }
    }
}