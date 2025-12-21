package ru.urasha.studygroup.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorsResponseDto(
        LocalDateTime timestamp,
        String message,
        List<ErrorDto> errors
) {
}
