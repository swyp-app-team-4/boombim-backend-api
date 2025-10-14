package boombimapi.domain.point.domain.repository;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.point.domain.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    Optional<Point> findByMember(Member member);
}
