package boombimapi.domain.search.domain.repository;

import boombimapi.domain.member.domain.entity.Member;
import boombimapi.domain.search.domain.entity.Search;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchRepository extends JpaRepository<Search, Long> {


    List<Search> findByMember(Member member);

    Optional<Search> findBySearchWord(String posName);
}
