package ru.urasha.studygroup.models.importing;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "import_operation")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "import_operation")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ImportStatus status;

    private String username;

    private String role;

    private String originalFilename;

    private String contentType;

    private Long fileSize;

    @Column(length = 512)
    private String objectKey;

    private Integer importedCount;

    @Column(length = 2000)
    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
