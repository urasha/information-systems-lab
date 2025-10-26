package ru.urasha.studygroup.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.studygroup.events.StudyGroupChangedEvent;
import ru.urasha.studygroup.exceptions.SameSourceAndTargetGroupException;
import ru.urasha.studygroup.exceptions.StudyGroupNotFoundException;
import ru.urasha.studygroup.models.StudyGroup;
import ru.urasha.studygroup.repositories.StudyGroupRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecialOpsService {

    private final StudyGroupRepository studyGroupRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<StudyGroup> searchByName(String substring) {
        return studyGroupRepository.findByNameContainingIgnoreCase(substring);
    }

    public List<String> getUniqueAdminNames() {
        return studyGroupRepository.findDistinctGroupAdminNames();
    }

    @Transactional
    public void deleteByAdminName(String adminName) {
        List<StudyGroup> groupsWithAdmin = studyGroupRepository.findByGroupAdmin_Name(adminName);

        if (groupsWithAdmin.isEmpty()) {
            return;
        }

        for (StudyGroup group : groupsWithAdmin) {
            Integer groupId = group.getId();
            studyGroupRepository.delete(group);
            eventPublisher.publishEvent(
                    new StudyGroupChangedEvent(groupId, StudyGroupChangedEvent.EventType.DELETED)
            );
        }
    }

    @Transactional
    public StudyGroup expelAllStudents(Integer groupId) {
        StudyGroup group = studyGroupRepository.findByIdForUpdate(groupId)
                .orElseThrow(() -> new StudyGroupNotFoundException(groupId));

        long expelledCount = group.getStudentsCount();
        group.setExpelledStudents(Math.max(group.getExpelledStudents(), 0) + expelledCount);
        group.setStudentsCount(0);

        StudyGroup savedGroup = studyGroupRepository.save(group);

        eventPublisher.publishEvent(
                new StudyGroupChangedEvent(savedGroup.getId(), StudyGroupChangedEvent.EventType.UPDATED)
        );

        return savedGroup;
    }

    @Transactional
    public Map<String, StudyGroup> transferAllStudents(Integer fromGroupId, Integer toGroupId) {
        if (fromGroupId.equals(toGroupId)) {
            throw new SameSourceAndTargetGroupException();
        }

        Integer firstLockId = Math.min(fromGroupId, toGroupId);
        Integer secondLockId = Math.max(fromGroupId, toGroupId);

        StudyGroup first = studyGroupRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new StudyGroupNotFoundException(firstLockId));
        StudyGroup second = studyGroupRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new StudyGroupNotFoundException(secondLockId));

        StudyGroup source = fromGroupId.equals(firstLockId) ? first : second;
        StudyGroup target = toGroupId.equals(firstLockId) ? first : second;

        int studentsToTransfer = source.getStudentsCount();

        target.setStudentsCount(target.getStudentsCount() + studentsToTransfer);
        source.setTransferredStudents(source.getTransferredStudents() + studentsToTransfer);
        source.setStudentsCount(0);

        StudyGroup savedSourceGroup = studyGroupRepository.save(source);
        StudyGroup savedTargetGroup = studyGroupRepository.save(target);

        eventPublisher.publishEvent(
                new StudyGroupChangedEvent(savedSourceGroup.getId(), StudyGroupChangedEvent.EventType.UPDATED)
        );
        eventPublisher.publishEvent(
                new StudyGroupChangedEvent(savedTargetGroup.getId(), StudyGroupChangedEvent.EventType.UPDATED)
        );

        return Map.of("from", savedSourceGroup, "to", savedTargetGroup);
    }
}
