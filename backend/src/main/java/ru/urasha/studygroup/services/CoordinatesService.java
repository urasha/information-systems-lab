package ru.urasha.studygroup.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.studygroup.models.Coordinates;
import ru.urasha.studygroup.repositories.CoordinatesRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoordinatesService {

    private final CoordinatesRepository coordinatesRepository;

    public List<Coordinates> getAll() {
        return coordinatesRepository.findAll();
    }
}
