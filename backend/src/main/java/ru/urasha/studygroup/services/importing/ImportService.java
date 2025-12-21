package ru.urasha.studygroup.services.importing;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.urasha.studygroup.dto.ImportOperationDto;
import ru.urasha.studygroup.dto.ImportResultDto;
import ru.urasha.studygroup.mappers.StudyGroupMapper;
import ru.urasha.studygroup.models.importing.ImportOperation;
import ru.urasha.studygroup.repositories.ImportOperationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportRetryService importRetryService;
    private final ImportOperationRepository importOperationRepository;
    private final StudyGroupMapper mapper;

    @Transactional(readOnly = true)
    public List<ImportOperationDto> listOperations(String username, String role) {
        List<ImportOperation> allOperations;
        if ("ADMIN".equalsIgnoreCase(role)) {
            allOperations = importOperationRepository.findAllByOrderByCreatedAtDesc();
        } else {
            allOperations = importOperationRepository.findByUsernameOrderByCreatedAtDesc(username);
        }

        return allOperations.stream()
                .map(operation -> new ImportOperationDto(
                        operation.getId(),
                        operation.getStatus() == null ? null : operation.getStatus().name(),
                        operation.getUsername(),
                        operation.getImportedCount(),
                        operation.getErrorMessage(),
                        operation.getCreatedAt(),
                        operation.getFinishedAt()
                ))
                .collect(Collectors.toList());
    }

    public ImportResultDto importFromFile(MultipartFile file, String username, String role) {
        return importRetryService.importFromFileWithRetry(file, username, role);
    }
}