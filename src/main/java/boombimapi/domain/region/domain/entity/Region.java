package boombimapi.domain.region.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@DynamicUpdate
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 날짜
    @Column(nullable = false)
    private LocalDate regionDate;

    // 시작 시간
    @Column(nullable = false)
    private LocalDateTime startTime;

    // 끝나는 시간
    @Column(nullable = false)
    private LocalDateTime endTime;

    // 장소
    @Column(nullable = false)
    private String posName;

    // 장소
    @Column(nullable = false)
    private String area;

    // 인원 수
    @Column(nullable = false)
    private Long peopleCnt;

}