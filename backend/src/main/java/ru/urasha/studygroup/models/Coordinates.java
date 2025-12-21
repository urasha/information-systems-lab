package ru.urasha.studygroup.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Setter
@Getter
@Entity
@Table(name = "coordinates")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "coordinates")
public class Coordinates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private double x;

    @NotNull
    @Max(498)
    private Integer y;
}
