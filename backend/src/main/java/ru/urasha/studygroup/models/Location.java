package ru.urasha.studygroup.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Setter
@Getter
@Entity
@Table(name = "location")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "location")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float x;

    @NotNull
    private Long y;

    private float z;
}
