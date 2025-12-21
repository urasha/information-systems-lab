package ru.urasha.studygroup.controllers.exceptions.handlers.global;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        });
        Map<String, Object> body = Map.of(
                "message", "Validation failed",
                "fieldErrors", fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }
}
