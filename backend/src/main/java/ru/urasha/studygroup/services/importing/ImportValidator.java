package ru.urasha.studygroup.services.importing;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.urasha.studygroup.dto.ErrorDto;
import ru.urasha.studygroup.dto.StudyGroupDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ImportValidator {

    private final Validator validator;

    public List<ErrorDto> validateAll(List<StudyGroupDto> dtos) {
        List<ErrorDto> errors = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            StudyGroupDto dto = dtos.get(i);
            Set<ConstraintViolation<StudyGroupDto>> violations = validator.validate(dto);
            for (ConstraintViolation<StudyGroupDto> v : violations) {
                String path = v.getPropertyPath().toString();
                errors.add(new ErrorDto(i, path, v.getMessage()));
            }
        }
        return errors;
    }
}