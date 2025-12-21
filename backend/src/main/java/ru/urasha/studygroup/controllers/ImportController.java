package ru.urasha.studygroup.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.urasha.studygroup.dto.ImportOperationDto;
import ru.urasha.studygroup.dto.ImportResultDto;
import ru.urasha.studygroup.services.importing.ImportService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping("/groups/import")
    public ResponseEntity<ImportResultDto> importFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User") String username,
            @RequestHeader(value = "X-Role") String role
    ) {
        ImportResultDto result = importService.importFromFile(file, username, role);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/imports")
    public ResponseEntity<List<ImportOperationDto>> listHistory(
            @RequestHeader(value = "X-User") String username,
            @RequestHeader(value = "X-Role") String role
    ) {
        List<ImportOperationDto> dtos = importService.listOperations(username, role);
        return ResponseEntity.ok(dtos);
    }
}
