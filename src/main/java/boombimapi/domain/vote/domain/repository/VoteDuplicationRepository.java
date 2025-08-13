package boombimapi.domain.vote.domain.repository;

import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.vote.domain.entity.Vote;
import boombimapi.domain.vote.domain.entity.VoteDuplication;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface VoteDuplicationRepository extends JpaRepository<VoteDuplication, Long> {
    List<VoteDuplication> findByUser(User user);

    @Query("select vd.user from VoteDuplication vd where vd.vote = :vote")
    List<User> findUsersByVote(@Param("vote") Vote vote);
}
