package ru.urasha.studygroup.models;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "study_group")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "study_group")
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Long version;

    @NotBlank
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "coordinates_id")
    @NotNull
    @Valid
    private Coordinates coordinates;

    @NotNull
    private LocalDate creationDate;

    @Min(0)
    private int studentsCount;

    @Min(1)
    private long expelledStudents;

    @Min(1)
    private int transferredStudents;

    @Enumerated(EnumType.STRING)
    private FormOfEducation formOfEducation;

    @NotNull
    @Min(1)
    private Integer shouldBeExpelled;

    @Min(1)
    private double averageMark;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Semester semesterEnum;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "group_admin_id")
    @NotNull
    @Valid
    private Person groupAdmin;

    @PrePersist
    public void prePersist() {
        if (creationDate == null) {
            creationDate = LocalDate.now();
        }
    }
}
