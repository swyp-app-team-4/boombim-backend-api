package boombimapi.domain.vote.domain.repository;

import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteAnswerRepository extends JpaRepository<VoteAnswer, Long> {
    Optional<VoteAnswer> findByUserAndVote(User user, Vote vote);
}
