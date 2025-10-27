package ru.urasha.studygroup.services.importing;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.urasha.studygroup.dto.ErrorDto;
import ru.urasha.studygroup.dto.ImportResultDto;
import ru.urasha.studygroup.dto.StudyGroupDto;
import ru.urasha.studygroup.exceptions.ImportException;
import ru.urasha.studygroup.exceptions.UniqueConstraintException;
import ru.urasha.studygroup.mappers.StudyGroupMapper;
import ru.urasha.studygroup.models.StudyGroup;
import ru.urasha.studygroup.repositories.StudyGroupRepository;
import ru.urasha.studygroup.services.UniqueConstraintService;
import ru.urasha.studygroup.util.JsonErrorParser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportTransactionalService {

    private static final Logger log = LoggerFactory.getLogger(ImportTransactionalService.class);

    private final StudyGroupRepository repository;
    private final UniqueConstraintService uniqueConstraintService;
    private final StudyGroupMapper mapper;
    private final ObjectMapper objectMapper;
    private final ImportValidator importValidator;
    private final ImportOperationService operationService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImportResultDto importFromFileTransactional(MultipartFile file, String username, String role) {
        if (file.isEmpty()) {
            throw new ImportException(Collections.singletonList(
                    new ErrorDto(-1, "file", "Empty or missing file")
            ));
        }

        String filename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();

        var operation = operationService.createRunningOperation(username, role);
        Long opId = operation.getId();

        try {
            List<StudyGroupDto> dtos = parseDtos(file, filename);

            List<ErrorDto> validationErrors = importValidator.validateAll(dtos);

            for (int i = 0; i < dtos.size(); i++) {
                try {
                    uniqueConstraintService.checkUniqueForCreate(dtos.get(i));
                } catch (UniqueConstraintException e) {
                    int index = i;
                    e.getErrors().forEach(err ->
                            validationErrors.add(new ErrorDto(index, err.field(), err.message()))
                    );
                }
            }

            if (!validationErrors.isEmpty()) {
                operationService.markFailed(
                        opId,
                        "Validation failed: " + validationErrors.size() + " error(s)"
                );
                throw new ImportException(validationErrors);
            }

            List<StudyGroup> entities = dtos.stream()
                    .map(mapper::toEntity)
                    .collect(Collectors.toList());

            repository.saveAll(entities);

            operationService.markCompleted(opId, entities.size());

            return new ImportResultDto(entities.size(), "Imported successfully");
        } catch (ImportException exception) {
            String shortMsg = "Import failed (validation or parse error)";
            operationService.markFailed(opId, shortMsg);
            throw exception;
        } catch (Exception ex) {
            log.warn("Import failed for user {}: {}", username, ex.getMessage());
            operationService.markFailed(opId, "Import failed: " + (ex.getMessage() == null ? "unknown error" : ex.getMessage()));
            throw new ImportException(Collections.singletonList(
                    new ErrorDto(-1, "file", "Import failed due to server error")
            ));
        }
    }

    private List<StudyGroupDto> parseDtos(MultipartFile file, String filename) {
        try {
            return objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<>() {
                    }
            );
        } catch (JsonParseException jpe) {
            log.warn("Invalid JSON structure in file '{}': {}", filename, jpe.getOriginalMessage());
            throw new ImportException(Collections.singletonList(
                    new ErrorDto(-1, "file", "Invalid JSON structure: check syntax (braces, commas, etc.)")
            ));
        } catch (JsonMappingException jme) {
            ErrorDto err = JsonErrorParser.fromJsonMappingException(jme);
            log.warn("JSON mapping error in file '{}': {} (field={}, index={})",
                    filename, jme.getOriginalMessage(), err.field(), err.index());
            throw new ImportException(Collections.singletonList(err));
        } catch (JsonProcessingException jpe) {
            log.error("JSON processing error in file '{}': {}", filename, jpe.getOriginalMessage());
            throw new ImportException(Collections.singletonList(
                    new ErrorDto(-1, "file", "Invalid JSON format")
            ));
        } catch (IOException ioe) {
            log.error("IO error while reading file '{}': {}", filename, ioe.getMessage());
            throw new ImportException(Collections.singletonList(
                    new ErrorDto(-1, "file", "Cannot read file")
            ));
        }
    }
}
