package ru.urasha.studygroup.services.studygroup;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.studygroup.dto.StudyGroupDto;
import ru.urasha.studygroup.events.StudyGroupChangedEvent;
import ru.urasha.studygroup.exceptions.StudyGroupNotFoundException;
import ru.urasha.studygroup.mappers.StudyGroupMapper;
import ru.urasha.studygroup.models.StudyGroup;
import ru.urasha.studygroup.repositories.StudyGroupRepository;
import ru.urasha.studygroup.services.UniqueConstraintService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyGroupTransactionalService {

    private final StudyGroupRepository repository;
    private final StudyGroupMapper studyGroupMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final UniqueConstraintService uniqueConstraintService;

    @Transactional
    public StudyGroup updateTransactional(Integer id, StudyGroupDto updatedGroupDto) throws StudyGroupNotFoundException {
        StudyGroup existingGroup = repository.findById(id)
                .orElseThrow(() -> new StudyGroupNotFoundException(id));

        uniqueConstraintService.checkUniqueForUpdate(id, updatedGroupDto);

        studyGroupMapper.updateEntityFromDto(updatedGroupDto, existingGroup);
        StudyGroup saved = repository.save(existingGroup);

        eventPublisher.publishEvent(
                new StudyGroupChangedEvent(saved.getId(), StudyGroupChangedEvent.EventType.UPDATED)
        );

        return saved;
    }
}
