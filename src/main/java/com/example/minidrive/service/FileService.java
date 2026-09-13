package com.example.minidrive.service;

import com.example.minidrive.entity.File;
import com.example.minidrive.entity.User;
import com.example.minidrive.repository.FileRepository;
import com.example.minidrive.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileService {

    private final S3Service s3Service;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public FileService(
            S3Service s3Service,
            FileRepository fileRepository,
            UserRepository userRepository) {

        this.s3Service = s3Service;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    public File uploadFile(MultipartFile file, String email) throws IOException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String filePath = s3Service.uploadFile(file, user.getId());

        File newFile = new File();

        newFile.setUser(user);
        newFile.setFileName(file.getOriginalFilename());
        newFile.setFilePath(filePath);
        newFile.setFileSize(file.getSize());

        String contentType = file.getContentType();

        if (contentType != null && contentType.contains("/")) {
            newFile.setFileType(contentType.substring(contentType.indexOf("/") + 1));
        } else {
            newFile.setFileType("unknown");
        }

        newFile.setIsDeleted(false);

        return fileRepository.save(newFile);
    }
    public java.util.List<File> getUserFiles(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return fileRepository.findByUserIdAndIsDeletedFalse(user.getId());
    }
    public File getFileForDownload(Long fileId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (file.getIsDeleted()) {
            throw new RuntimeException("File has been deleted");
        }

        return file;
    }
}