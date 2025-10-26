package ru.urasha.studygroup.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.urasha.studygroup.models.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Integer> {

    Page<StudyGroup> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<StudyGroup> findByGroupAdmin_Name(String name);

    List<StudyGroup> findByNameContainingIgnoreCase(String substring);

    @Query("select distinct g.groupAdmin.name from StudyGroup g where g.groupAdmin.name is not null")
    List<String> findDistinctGroupAdminNames();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from StudyGroup g where g.id = :id")
    Optional<StudyGroup> findByIdForUpdate(@Param("id") Integer id);

    Optional<StudyGroup> findByCoordinates_XAndCoordinates_Y(Double x, Integer y);

    Optional<StudyGroup> findByGroupAdmin_PassportID(@Param("passport") String passport);

    Optional<StudyGroup> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
