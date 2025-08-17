package boombimapi.domain.vote.domain.repository;


import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteAnswer;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VoteAnswerRepository extends JpaRepository<VoteAnswer, Long> {
    Optional<VoteAnswer> findByMemberAndVote(Member user, Vote vote);

    List<VoteAnswer> findByMember(Member member);

    List<VoteAnswer> findByVote(Vote vote);

    @Query("select va.member from VoteAnswer va where va.vote = :vote")
    List<Member> findMembersByVote(@Param("vote") Vote vote);
}
