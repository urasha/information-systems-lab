package ru.urasha.studygroup.controllers.exceptions.handlers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.urasha.studygroup.controllers.ImportController;
import ru.urasha.studygroup.dto.ErrorsResponseDto;
import ru.urasha.studygroup.exceptions.ImportException;

import java.time.LocalDateTime;

@RestControllerAdvice(assignableTypes = ImportController.class)
public class ImportExceptionHandler {

    @ExceptionHandler(ImportException.class)
    public ResponseEntity<ErrorsResponseDto> handleImportException(ImportException exception) {
        ErrorsResponseDto body = new ErrorsResponseDto(
                LocalDateTime.now(),
                exception.getMessage(),
                exception.getErrors()
        );
        return ResponseEntity.badRequest().body(body);
    }
}
