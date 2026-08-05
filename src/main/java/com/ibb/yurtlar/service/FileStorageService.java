package com.ibb.yurtlar.service;

import com.ibb.yurtlar.config.FileStorageProperties;
import com.ibb.yurtlar.dto.StoredFileInfo;
import com.ibb.yurtlar.exception.EmptyFileException;
import com.ibb.yurtlar.exception.FileStorageException;
import com.ibb.yurtlar.exception.InvalidFileTypeException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.ibb.yurtlar.exception.InvalidFileSizeException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;

@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Map<String, String>
            ALLOWED_CONTENT_TYPES = Map.of(
            "application/pdf", ".pdf",
            "image/jpeg", ".jpg",
            "image/png", ".png"
    );

    private final Path uploadRoot;

    public FileStorageService(
            FileStorageProperties properties
    ) {
        this.uploadRoot = Path
                .of(properties.getUploadDir())
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new FileStorageException(
                    "Dosya yükleme klasörü oluşturulamadı.",
                    exception
            );
        }
    }

    public StoredFileInfo storeStudentDocument(
            MultipartFile file,
            Long admissionId,
            Long documentTypeId
    ) {
        validateFile(file);

        String originalFileName =
                StringUtils.cleanPath(
                        file.getOriginalFilename() == null
                                ? "document"
                                : file.getOriginalFilename()
                );

        String contentType = file.getContentType();
        String extension =
                ALLOWED_CONTENT_TYPES.get(contentType);

        String storedFileName =
                UUID.randomUUID() + extension;

        Path documentDirectory = uploadRoot
                .resolve("student-documents")
                .resolve("admission-" + admissionId)
                .resolve("document-type-" + documentTypeId)
                .normalize();

        Path destination = documentDirectory
                .resolve(storedFileName)
                .normalize();

        ensurePathIsInsideUploadRoot(destination);

        try {
            Files.createDirectories(documentDirectory);

            try (InputStream inputStream =
                         file.getInputStream()) {

                Files.copy(
                        inputStream,
                        destination,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (IOException exception) {
            throw new FileStorageException(
                    "Dosya kaydedilirken bir hata oluştu.",
                    exception
            );
        }

        String relativePath = uploadRoot
                .relativize(destination)
                .toString();

        return new StoredFileInfo(
                originalFileName,
                storedFileName,
                relativePath,
                contentType,
                file.getSize()
        );
    }

    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        Path filePath = uploadRoot
                .resolve(relativePath)
                .normalize();

        ensurePathIsInsideUploadRoot(filePath);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new FileStorageException(
                    "Eski dosya silinemedi.",
                    exception
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new EmptyFileException();
        }


        if (file.getSize() > MAX_FILE_SIZE) {
            /*
            throw new IllegalArgumentException
            ("Dosya boyutu en fazla 5 MB olabilir.");
            */
            throw new InvalidFileSizeException();
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.containsKey(contentType)) {

            throw new InvalidFileTypeException(contentType);
        }
    }

    private void ensurePathIsInsideUploadRoot(Path path) {
        if (!path.startsWith(uploadRoot)) {
            throw new FileStorageException(
                    "Geçersiz dosya yolu tespit edildi.",
                    null
            );
        }
    }

    public Resource loadAsResource(String relativePath) {
        Path filePath = uploadRoot
                .resolve(relativePath)
                .normalize();

        ensurePathIsInsideUploadRoot(filePath);

        try {
            Resource resource = new UrlResource(
                    filePath.toUri()
            );

            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException(
                        "Dosya bulunamadı veya okunamıyor.",
                        null
                );
            }

            return resource;

        } catch (MalformedURLException exception) {
            throw new FileStorageException(
                    "Dosya yolu okunamadı.",
                    exception
            );
        }
    }
}