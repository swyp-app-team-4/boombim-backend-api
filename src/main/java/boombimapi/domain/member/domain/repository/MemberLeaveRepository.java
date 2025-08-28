package boombimapi.domain.member.domain.repository;

import boombimapi.domain.member.domain.entity.MemberLeave;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberLeaveRepository extends JpaRepository<MemberLeave, Long> {
}
