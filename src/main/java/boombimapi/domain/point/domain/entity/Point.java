package boombimapi.domain.point.domain.entity;

import boombimapi.domain.member.domain.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@DynamicUpdate
@Table(name = "point")
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "balance", nullable = false)
    private Long balance;


    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Point(Member member) {
        this.member = member;
        this.balance = 0L;
    }


    public void addBalance(Long amount) {
        this.balance += amount;
    }

    public void subtractBalance(Long amount) {
        this.balance -= amount;
    }

}
