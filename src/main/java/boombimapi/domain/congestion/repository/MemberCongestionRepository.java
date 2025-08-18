package boombimapi.domain.congestion.repository;

import boombimapi.domain.congestion.entity.MemberCongestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCongestionRepository extends JpaRepository<MemberCongestion, Long> {

}
