package ru.urasha.studygroup.services.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.urasha.studygroup.dto.ImportResultDto;
import ru.urasha.studygroup.exceptions.SerializableImportException;

@Service
@RequiredArgsConstructor
public class ImportRetryService {

    private final ImportTransactionalService importTransactionalService;

    @Retryable(
            retryFor = {CannotAcquireLockException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 1000)
    )
    public ImportResultDto importFromFileWithRetry(MultipartFile file, String username, String role) {
        return importTransactionalService.importFromFileTransactional(file, username, role);
    }

    @Recover
    public ImportResultDto recoverImportFromFile(CannotAcquireLockException e,
                                                 MultipartFile file, String username, String role) {
        throw new SerializableImportException();
    }
}
