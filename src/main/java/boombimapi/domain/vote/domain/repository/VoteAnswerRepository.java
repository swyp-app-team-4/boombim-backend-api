package boombimapi.domain.vote.domain.repository;

import boombimapi.domain.vote.domain.entity.VoteAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteAnswerRepository extends JpaRepository<VoteAnswer, Long> {
}
