package com.example.minidrive.repository;

import com.example.minidrive.entity.FileShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileShareRepository extends JpaRepository<FileShare, Long> {

    List<FileShare> findBySharedWithUserId(Long userId);

    Optional<FileShare> findByFileIdAndSharedWithUserId(
            Long fileId,
            Long userId
    );
}