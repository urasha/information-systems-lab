package ru.urasha.studygroup.services.importing;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.urasha.studygroup.config.MinioProperties;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImportStorageService.class);

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public String storeImportFile(Long operationId, String originalFilename, String contentType, byte[] content) {
        if (!properties.isEnabled()) {
            return null;
        }

        String safeName = sanitizeFilename(originalFilename == null ? "upload" : originalFilename);
        String objectKey = "imports/" + operationId + "/" + UUID.randomUUID() + "-" + safeName;

        try (InputStream stream = new ByteArrayInputStream(content)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectKey)
                            .contentType(contentType == null ? "application/octet-stream" : contentType)
                            .stream(stream, content.length, -1)
                            .build()
            );
        } catch (Exception ex) {
            log.error("Failed to upload file to MinIO: {}", ex.getMessage());
            throw new RuntimeException("Failed to store import file in MinIO", ex);
        }

        registerRollbackCleanup(objectKey);

        return objectKey;
    }

    public StoredFile download(String objectKey, String originalFilename, String contentType, long fileSize) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("MinIO integration is disabled");
        }

        try {
            var response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectKey)
                            .build()
            );

            InputStreamResource resource = new InputStreamResource(response);
            return new StoredFile(resource, originalFilename, contentType, fileSize);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to download import file", ex);
        }
    }

    private void registerRollbackCleanup(String objectKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    deleteQuietly(objectKey);
                }
            }
        });
    }

    private void deleteQuietly(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectKey)
                            .build()
            );
            log.info("Rolled back uploaded file '{}' from MinIO", objectKey);
        } catch (Exception ex) {
            log.warn("Failed to rollback MinIO object '{}': {}", objectKey, ex.getMessage());
        }
    }

    private String sanitizeFilename(String name) {
        String ascii = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (ascii.isBlank()) {
            return "upload";
        }
        byte[] bytes = ascii.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public record StoredFile(InputStreamResource resource, String originalFilename, String contentType, long size) {
    }
}
