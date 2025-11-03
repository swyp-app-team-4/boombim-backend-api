package boombimapi.domain.point.domain.repository;

import boombimapi.domain.point.domain.entity.EventCampaign;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCampaignRepository extends JpaRepository<EventCampaign, Long> {

    Optional<EventCampaign> findTopByOrderByCreatedAtDesc();
}
