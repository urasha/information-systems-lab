package ru.urasha.studygroup.services.importing;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.studygroup.dto.ImportOperationDto;
import ru.urasha.studygroup.dto.ImportResultDto;
import ru.urasha.studygroup.models.importing.ImportOperation;
import ru.urasha.studygroup.repositories.ImportOperationRepository;
import ru.urasha.studygroup.services.importing.ImportStorageService.StoredFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportRetryService importRetryService;
    private final ImportOperationRepository importOperationRepository;
    private final ImportStorageService importStorageService;

    @Transactional(readOnly = true)
    public List<ImportOperationDto> listOperations(String username, String role) {
        List<ImportOperation> allOperations;
        if ("ADMIN".equalsIgnoreCase(role)) {
            allOperations = importOperationRepository.findAllByOrderByCreatedAtDesc();
        } else {
            allOperations = importOperationRepository.findByUsernameOrderByCreatedAtDesc(username);
        }

        return allOperations.stream()
                .map(operation -> new ImportOperationDto(
                        operation.getId(),
                        operation.getStatus() == null ? null : operation.getStatus().name(),
                        operation.getUsername(),
                        operation.getImportedCount(),
                        operation.getErrorMessage(),
                        operation.getCreatedAt(),
                    operation.getFinishedAt(),
                    operation.getOriginalFilename(),
                    operation.getFileSize(),
                    operation.getContentType(),
                    operation.getObjectKey()
                ))
                .collect(Collectors.toList());
    }

    public ImportResultDto importFromFile(MultipartFile file, String username, String role) {
        return importRetryService.importFromFileWithRetry(file, username, role);
    }

    @Transactional(readOnly = true)
    public StoredFile getImportFile(Long id, String username, String role) {
        var operation = importOperationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Import operation not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        if (!isAdmin && !operation.getUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied to import file");
        }

        if (operation.getObjectKey() == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Import file not stored or already cleaned up");
        }

        return importStorageService.download(
                operation.getObjectKey(),
                operation.getOriginalFilename(),
                operation.getContentType(),
                operation.getFileSize() == null ? -1 : operation.getFileSize()
        );
    }
}