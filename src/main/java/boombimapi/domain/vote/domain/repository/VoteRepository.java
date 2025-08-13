package boombimapi.domain.vote.domain.repository;

import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.vote.domain.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByPosId(String posId);

    List<Vote> findByUser(User user);

    // 위도/경도 바운딩 박스 안에 들어오는 후보만 가져오기
    @Query("SELECT v FROM Vote v " +
            "WHERE v.latitude BETWEEN :latMin AND :latMax " +
            "AND v.longitude BETWEEN :lonMin AND :lonMax")
    List<Vote> findAllInBoundingBox(double latMin, double latMax,
                                    double lonMin, double lonMax);
}
