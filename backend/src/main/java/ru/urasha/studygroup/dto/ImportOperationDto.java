package ru.urasha.studygroup.dto;

import java.time.LocalDateTime;

public record ImportOperationDto(
        Long id,
        String status,
        String username,
        Integer importedCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime finishedAt
) {
}
