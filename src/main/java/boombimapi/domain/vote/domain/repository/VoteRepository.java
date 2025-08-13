package boombimapi.domain.vote.domain.repository;

import boombimapi.domain.vote.domain.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByPosId(String posId);
}
