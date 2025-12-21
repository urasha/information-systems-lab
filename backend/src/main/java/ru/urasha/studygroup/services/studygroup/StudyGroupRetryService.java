package ru.urasha.studygroup.services.studygroup;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.urasha.studygroup.dto.StudyGroupDto;
import ru.urasha.studygroup.exceptions.ExceptionMessages;
import ru.urasha.studygroup.exceptions.OptimisticLockException;
import ru.urasha.studygroup.exceptions.SerializableCreateException;
import ru.urasha.studygroup.models.StudyGroup;

@Service
@RequiredArgsConstructor
public class StudyGroupRetryService {

    private final StudyGroupTransactionalService studyGroupTransactionalService;

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 200, multiplier = 2,  maxDelay = 1000)
    )
    public StudyGroup updateWithRetry(Integer id, StudyGroupDto dto) {
        return studyGroupTransactionalService.updateTransactional(id, dto);
    }

    @Retryable(
            retryFor = {CannotAcquireLockException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 1000)
    )
    public StudyGroup createWithRetry(StudyGroupDto dto) {
        return studyGroupTransactionalService.createTransactional(dto);
    }

    @Recover
    public StudyGroup recoverUpdate(ObjectOptimisticLockingFailureException e,
                                    Integer id, StudyGroupDto dto) {
        throw new OptimisticLockException(
                String.format(ExceptionMessages.GROUP_WAS_MODIFIED.getMessage(), id)
        );
    }

    @Recover
    public StudyGroup recoverCreate(CannotAcquireLockException e,
                                    StudyGroupDto dto) {
        throw new SerializableCreateException();
    }
}
