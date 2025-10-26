package ru.urasha.studygroup.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.studygroup.models.Person;
import ru.urasha.studygroup.repositories.PersonRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepository personRepository;

    public List<Person> getAll() {
        return personRepository.findAll();
    }
}
