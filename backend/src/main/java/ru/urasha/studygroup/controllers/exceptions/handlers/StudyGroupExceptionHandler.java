package ru.urasha.studygroup.controllers.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.urasha.studygroup.controllers.StudyGroupController;
import ru.urasha.studygroup.dto.DefaultErrorResponseDto;
import ru.urasha.studygroup.dto.ErrorsResponseDto;
import ru.urasha.studygroup.exceptions.OptimisticLockException;
import ru.urasha.studygroup.exceptions.SerializableCreateException;
import ru.urasha.studygroup.exceptions.StudyGroupException;
import ru.urasha.studygroup.exceptions.UniqueConstraintException;

import java.time.LocalDateTime;

@RestControllerAdvice(assignableTypes = StudyGroupController.class)
public class StudyGroupExceptionHandler {

    @ExceptionHandler(StudyGroupException.class)
    public ResponseEntity<DefaultErrorResponseDto> handleStudyGroupException(StudyGroupException exception) {
        DefaultErrorResponseDto body = new DefaultErrorResponseDto(
                LocalDateTime.now(),
                exception.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UniqueConstraintException.class)
    public ResponseEntity<ErrorsResponseDto> handleUniqueConstraint(UniqueConstraintException exception) {
        ErrorsResponseDto body = new ErrorsResponseDto(
                LocalDateTime.now(),
                exception.getMessage(),
                exception.getErrors()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<DefaultErrorResponseDto> handleOptimisticLock(OptimisticLockException exception) {
        DefaultErrorResponseDto body = new DefaultErrorResponseDto(
                LocalDateTime.now(),
                exception.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(SerializableCreateException.class)
    public ResponseEntity<DefaultErrorResponseDto> handleOptimisticLock(SerializableCreateException exception) {
        DefaultErrorResponseDto body = new DefaultErrorResponseDto(
                LocalDateTime.now(),
                exception.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
