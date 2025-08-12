package boombimapi.domain.vote.domain.entity;

import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@DynamicUpdate
public class VoteAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    // 투표 타입
    @Column(nullable = false)
    private VoteAnswerType answerType;


    @Builder
    public VoteAnswer(User user, Vote vote, VoteAnswerType answerType){
        this.user=user;
        this.vote=vote;
        this.answerType=answerType;
    }

}

