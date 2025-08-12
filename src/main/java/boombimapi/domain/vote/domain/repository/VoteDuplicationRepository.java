package boombimapi.domain.vote.domain.repository;

import boombimapi.domain.vote.domain.entity.VoteDuplication;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VoteDuplicationRepository extends JpaRepository<VoteDuplication, Long> {
}
