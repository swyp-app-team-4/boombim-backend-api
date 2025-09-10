package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.CongestionLevel;
import boombimapi.domain.vote.domain.entity.type.VoteAnswerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CongestionLevelRepository extends JpaRepository<CongestionLevel, Integer> {

    Optional<CongestionLevel> findByName(String name);
}
