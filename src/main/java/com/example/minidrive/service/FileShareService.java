package com.example.minidrive.service;

import com.example.minidrive.entity.File;
import com.example.minidrive.entity.FileShare;
import com.example.minidrive.entity.User;
import com.example.minidrive.repository.FileRepository;
import com.example.minidrive.repository.FileShareRepository;
import com.example.minidrive.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class FileShareService {

    private final FileShareRepository fileShareRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    public FileShareService(
            FileShareRepository fileShareRepository,
            FileRepository fileRepository,
            UserRepository userRepository) {

        this.fileShareRepository = fileShareRepository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    public FileShare shareFile(
            Long fileId,
            String ownerEmail,
            String sharedWithEmail,
            String permission) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(owner.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (file.getIsDeleted()) {
            throw new RuntimeException("File has been deleted");
        }

        User sharedWithUser = userRepository.findByEmail(sharedWithEmail)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        FileShare fileShare = new FileShare();

        fileShare.setFile(file);
        fileShare.setSharedWithUser(sharedWithUser);
        fileShare.setPermission(permission);

        return fileShareRepository.save(fileShare);
    }
    public java.util.List<FileShare> getSharedFiles(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return fileShareRepository.findBySharedWithUserId(user.getId());
    }
    public File getFileForDownload(Long fileId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (file.getIsDeleted()) {
            throw new RuntimeException("File has been deleted");
        }

        // Owner can download
        if (file.getUser().getId().equals(user.getId())) {
            return file;
        }

        // Check whether the file was shared with this user
        FileShare fileShare = fileShareRepository
                .findByFileIdAndSharedWithUserId(fileId, user.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

        // Only VIEW permission allows downloading
        if (!"VIEW".equalsIgnoreCase(fileShare.getPermission())) {
            throw new RuntimeException("Access denied");
        }

        return file;
    }
}