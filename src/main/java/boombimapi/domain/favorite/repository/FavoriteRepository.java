package boombimapi.domain.favorite.repository;

import boombimapi.domain.favorite.entity.Favorite;
import java.util.List;

import boombimapi.domain.member.domain.entity.Member;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByMemberIdAndMemberPlaceId(
        String memberId,
        Long memberPlaceId
    );

    void deleteByMemberIdAndMemberPlaceId(
        String memberId,
        Long memberPlaceId
    );

    List<Favorite> findAllByMemberId(
        String memberId
    );


    @Query("SELECT f FROM Favorite f " +
            "JOIN FETCH f.memberPlace mp " +
            "JOIN FETCH mp.memberCongestions mc " +
            "JOIN FETCH mc.congestionLevel cl " +
            "WHERE f.member = :member")
    List<Favorite> findByMemberWithJoin(@Param("member") Member member);


}
