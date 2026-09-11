package com.example.minidrive.repository;

import com.example.minidrive.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByUserIdAndIsDeletedFalse(Long userId);

}