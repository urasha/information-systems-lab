package ru.urasha.studygroup.controllers.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.urasha.studygroup.controllers.SpecialOpsController;
import ru.urasha.studygroup.dto.DefaultErrorResponseDto;
import ru.urasha.studygroup.exceptions.SameSourceAndTargetGroupException;
import ru.urasha.studygroup.exceptions.StudyGroupException;
import ru.urasha.studygroup.exceptions.StudyGroupNotFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice(assignableTypes = SpecialOpsController.class)
public class SpecialOpsExceptionHandler {

    @ExceptionHandler(StudyGroupException.class)
    public ResponseEntity<DefaultErrorResponseDto> handleStudyGroupException(StudyGroupException exception) {
        DefaultErrorResponseDto body = new DefaultErrorResponseDto(
                LocalDateTime.now(),
                exception.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(SameSourceAndTargetGroupException.class)
    public ResponseEntity<DefaultErrorResponseDto> handleSameSourceTarget(SameSourceAndTargetGroupException exception) {
        DefaultErrorResponseDto body = new DefaultErrorResponseDto(
                LocalDateTime.now(),
                exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(StudyGroupNotFoundException.class)
    public ResponseEntity<DefaultErrorResponseDto> handleNotFound(StudyGroupNotFoundException exception) {
        DefaultErrorResponseDto body = new DefaultErrorResponseDto(
                LocalDateTime.now(),
                exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
