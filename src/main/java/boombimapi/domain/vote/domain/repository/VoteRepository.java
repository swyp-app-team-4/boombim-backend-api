package boombimapi.domain.vote.domain.repository;

import boombimapi.domain.vote.domain.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
}
