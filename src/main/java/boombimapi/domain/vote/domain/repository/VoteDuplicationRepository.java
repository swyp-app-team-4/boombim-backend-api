package boombimapi.domain.vote.domain.repository;


import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteDuplication;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface VoteDuplicationRepository extends JpaRepository<VoteDuplication, Long> {
    List<VoteDuplication> findByMember(Member member);

    @Query("select vd.member from VoteDuplication vd where vd.vote = :vote")
    List<Member> findMembersByVote(@Param("vote") Vote vote);
}
