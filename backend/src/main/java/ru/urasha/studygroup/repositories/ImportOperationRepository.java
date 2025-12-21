package ru.urasha.studygroup.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.urasha.studygroup.models.importing.ImportOperation;

import java.util.List;

public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {

    List<ImportOperation> findByUsernameOrderByCreatedAtDesc(String username);

    List<ImportOperation> findAllByOrderByCreatedAtDesc();
}
