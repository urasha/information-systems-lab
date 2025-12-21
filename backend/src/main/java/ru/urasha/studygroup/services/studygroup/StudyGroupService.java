package ru.urasha.studygroup.services.studygroup;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.studygroup.dto.StudyGroupDto;
import ru.urasha.studygroup.events.StudyGroupChangedEvent;
import ru.urasha.studygroup.exceptions.StudyGroupNotFoundException;
import ru.urasha.studygroup.models.StudyGroup;
import ru.urasha.studygroup.repositories.StudyGroupRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final StudyGroupRetryService studyGroupRetryService;

    @Transactional(readOnly = true)
    public Page<StudyGroup> getGroupPage(String nameContains, Pageable pageable) {
        return nameContains == null || nameContains.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCase(nameContains, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<StudyGroup> get(Integer id) {
        return repository.findById(id);
    }

    public StudyGroup create(StudyGroupDto dto) {
        return studyGroupRetryService.createWithRetry(dto);
    }

    public StudyGroup update(Integer id, StudyGroupDto updatedGroupDto) throws StudyGroupNotFoundException {
        return studyGroupRetryService.updateWithRetry(id, updatedGroupDto);
    }

    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);

        eventPublisher.publishEvent(
                new StudyGroupChangedEvent(id, StudyGroupChangedEvent.EventType.DELETED)
        );
    }
}
