package ru.urasha.studygroup.exceptions;

import lombok.Getter;
import ru.urasha.studygroup.dto.ErrorDto;

import java.util.List;

@Getter
public class ImportException extends RuntimeException {
    private final List<ErrorDto> errors;

    public ImportException(List<ErrorDto> errors) {
        super("Import failed: " + (errors == null ? 0 : errors.size()) + " error(s)");
        this.errors = errors;
    }

}
