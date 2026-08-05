package com.ibb.yurtlar.dto;

public record StoredFileInfo(
        String originalFileName,
        String storedFileName,
        String relativePath,
        String contentType,
        long fileSize
) {
}