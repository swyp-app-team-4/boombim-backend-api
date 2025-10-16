package boombimapi.domain.point.domain.repository;

import boombimapi.domain.point.domain.entity.EventCampaign;
import boombimapi.domain.point.domain.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
}
