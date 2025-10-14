package boombimapi.domain.point.domain.repository;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.point.domain.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findAllByMemberOrderByCreatedAtDesc(Member member);
}
