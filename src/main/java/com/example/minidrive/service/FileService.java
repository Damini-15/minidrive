package com.example.minidrive.service;

import com.example.minidrive.entity.File;
import com.example.minidrive.entity.FileShare;
import com.example.minidrive.entity.User;
import com.example.minidrive.repository.FileRepository;
import com.example.minidrive.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.minidrive.repository.FileShareRepository;
import java.io.IOException;

@Service
public class FileService {

    private final S3Service s3Service;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileShareRepository fileShareRepository;
    public FileService(
            S3Service s3Service,
            FileRepository fileRepository,
            UserRepository userRepository,
            FileShareRepository fileShareRepository) {

        this.s3Service = s3Service;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.fileShareRepository = fileShareRepository;
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

        // File must not be deleted
        if (file.getIsDeleted()) {
            throw new RuntimeException("File has been deleted");
        }

        // Owner can download
        if (file.getUser().getId().equals(user.getId())) {
            return file;
        }

        // Check if the file was shared with this user
        FileShare fileShare = fileShareRepository
                .findByFileIdAndSharedWithUserId(fileId, user.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

        // Only VIEW permission allows downloading
        if (!"VIEW".equalsIgnoreCase(fileShare.getPermission())) {
            throw new RuntimeException("Access denied");
        }

        return file;
    }
    public void deleteFile(Long fileId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (file.getIsDeleted()) {
            throw new RuntimeException("File already deleted");
        }

        file.setIsDeleted(true);

        fileRepository.save(file);
    }
}