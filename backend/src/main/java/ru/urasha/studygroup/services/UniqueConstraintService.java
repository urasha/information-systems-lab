package ru.urasha.studygroup.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.studygroup.dto.ErrorDto;
import ru.urasha.studygroup.dto.StudyGroupDto;
import ru.urasha.studygroup.exceptions.UniqueConstraintException;
import ru.urasha.studygroup.models.StudyGroup;
import ru.urasha.studygroup.repositories.StudyGroupRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniqueConstraintService {

    private final StudyGroupRepository groupRepository;

    public void checkUniqueForCreate(StudyGroupDto dto) {
        List<ErrorDto> errors = new ArrayList<>();

        if (dto.getName() != null && groupRepository.existsByNameIgnoreCase(dto.getName())) {
            errors.add(new ErrorDto(-1, "name", "Group name must be unique"));
        }

        if (dto.getCoordinates() != null) {
            double x = dto.getCoordinates().getX();
            Integer y = dto.getCoordinates().getY();
            Optional<StudyGroup> existing = groupRepository.findByCoordinates_XAndCoordinates_Y(x, y);
            if (existing.isPresent()) {
                errors.add(new ErrorDto(-1, "coordinates", "Coordinates (x,y) must be unique"));
            }
        }

        if (dto.getGroupAdmin() != null && dto.getGroupAdmin().getPassportID() != null) {
            String passport = dto.getGroupAdmin().getPassportID();
            Optional<StudyGroup> existing = groupRepository.findByGroupAdmin_PassportID(passport);
            if (existing.isPresent()) {
                errors.add(new ErrorDto(-1, "groupAdmin.passportID", "passportID must be unique among group admins"));
            }
        }

        if (!errors.isEmpty()) {
            throw new UniqueConstraintException(errors);
        }
    }

    public void checkUniqueForUpdate(Integer id, StudyGroupDto dto) {
        List<ErrorDto> errors = new ArrayList<>();

        if (dto.getName() != null) {
            Optional<StudyGroup> byName = groupRepository.findByNameIgnoreCase(dto.getName());
            if (byName.isPresent() && !byName.get().getId().equals(id)) {
                errors.add(new ErrorDto(-1, "name", "Group name must be unique"));
            }
        }

        if (dto.getCoordinates() != null) {
            double x = dto.getCoordinates().getX();
            Integer y = dto.getCoordinates().getY();
            Optional<StudyGroup> existing = groupRepository.findByCoordinates_XAndCoordinates_Y(x, y);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                errors.add(new ErrorDto(-1, "coordinates", "Coordinates (x,y) must be unique"));
            }
        }

        if (dto.getGroupAdmin() != null && dto.getGroupAdmin().getPassportID() != null) {
            String passport = dto.getGroupAdmin().getPassportID();
            Optional<StudyGroup> existing = groupRepository.findByGroupAdmin_PassportID(passport);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                errors.add(new ErrorDto(-1, "groupAdmin.passportID", "passportID must be unique among group admins"));
            }
        }

        if (!errors.isEmpty()) {
            throw new UniqueConstraintException(errors);
        }
    }

}
