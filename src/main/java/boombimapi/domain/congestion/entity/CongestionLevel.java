package boombimapi.domain.congestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "congestion_levels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CongestionLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, nullable = false, unique = true)
    private String name;

    @Column(length = 100, nullable = false)
    private String message;

    @Builder
    private CongestionLevel(
        String name,
        String message
    ) {
        this.name = name;
        this.message = message;
    }

    private static CongestionLevel of(
        String name,
        String message
    ) {
        return CongestionLevel.builder()
            .name(name)
            .message(message)
            .build();
    }

}
