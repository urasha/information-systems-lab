package ru.urasha.studygroup.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.urasha.studygroup.dto.StudyGroupDto;
import ru.urasha.studygroup.models.StudyGroup;
import ru.urasha.studygroup.services.studygroup.StudyGroupService;


@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @GetMapping
    public Page<StudyGroup> list(
            @RequestParam(required = false) String nameContains,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "true") boolean asc
    ) {
        Sort.Direction sortDirection = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable groupPage = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return studyGroupService.getGroupPage(nameContains, groupPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyGroup> get(@PathVariable Integer id) {
        return studyGroupService.get(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudyGroup> create(@Valid @RequestBody StudyGroupDto groupDto) {
        StudyGroup saved = studyGroupService.create(groupDto);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyGroup> update(@PathVariable Integer id,
                                             @Valid @RequestBody StudyGroupDto groupDto) {
        StudyGroup updated = studyGroupService.update(id, groupDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        studyGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
