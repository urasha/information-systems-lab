package ru.urasha.studygroup.services.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.studygroup.models.importing.ImportOperation;
import ru.urasha.studygroup.models.importing.ImportStatus;
import ru.urasha.studygroup.repositories.ImportOperationRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ImportOperationService {

    private final ImportOperationRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportOperation createRunningOperation(String username, String role,
                                                  String originalFilename, String contentType, long fileSize) {
        ImportOperation importOperation = ImportOperation.builder()
                .status(ImportStatus.RUNNING)
                .username(username)
                .role(role)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .fileSize(fileSize)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(importOperation);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void attachObjectKey(Long operationId, String objectKey) {
        if (objectKey == null) {
            return;
        }
        repository.findById(operationId).ifPresent(importOperation -> {
            importOperation.setObjectKey(objectKey);
            repository.save(importOperation);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(Long operationId, int importedCount) {
        repository.findById(operationId).ifPresent(importOperation -> {
            importOperation.setStatus(ImportStatus.COMPLETED);
            importOperation.setImportedCount(importedCount);
            importOperation.setFinishedAt(LocalDateTime.now());
            repository.save(importOperation);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long operationId, String shortErrorMessage) {
        repository.findById(operationId).ifPresent(importOperation -> {
            importOperation.setStatus(ImportStatus.FAILED);
            importOperation.setErrorMessage(shortErrorMessage);
            importOperation.setFinishedAt(LocalDateTime.now());
            repository.save(importOperation);
        });
    }
}
