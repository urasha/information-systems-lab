package ru.urasha.studygroup.exceptions;

import lombok.Getter;
import ru.urasha.studygroup.dto.ErrorDto;

import java.util.List;

@Getter
public class UniqueConstraintException extends RuntimeException {

    private final List<ErrorDto> errors;

    public UniqueConstraintException(List<ErrorDto> errors) {
        super("Unique constraints validation failed");
        this.errors = errors;
    }
}