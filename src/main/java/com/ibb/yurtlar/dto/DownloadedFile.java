package com.ibb.yurtlar.dto;

import org.springframework.core.io.Resource;

public record DownloadedFile(
        Resource resource,
        String originalFileName,
        String contentType
) {
}