package boombimapi.domain.vote.domain.repository;

import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.vote.domain.entity.VoteDuplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface VoteDuplicationRepository extends JpaRepository<VoteDuplication, Long> {
    List<VoteDuplication> findByUser(User user);
}
