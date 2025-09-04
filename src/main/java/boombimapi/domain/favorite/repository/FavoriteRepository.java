package boombimapi.domain.favorite.repository;

import boombimapi.domain.favorite.entity.Favorite;
import boombimapi.domain.place.entity.MemberPlace;
import boombimapi.domain.place.entity.PlaceType;
import java.util.List;
import java.util.Optional;

import boombimapi.domain.member.domain.entity.Member;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByMemberIdAndPlaceIdAndPlaceType(
        String memberId,
        Long placeId,
        PlaceType placeType
    );

    void deleteByMemberIdAndPlaceIdAndPlaceType(
        String memberId,
        Long placeId,
        PlaceType placeType
    );

    List<Favorite> findAllByMemberId(
        String memberId
    );

    // 즐겨 찾기 여부
    Optional<Favorite> findByMemberAndPlaceIdAndPlaceType(
            Member member,
            Long placeId,
            PlaceType placeType
    );

//    @Query("SELECT f FROM Favorite f " +
//        "JOIN FETCH f.memberPlace mp " +
//        "JOIN FETCH mp.memberCongestions mc " +
//        "JOIN FETCH mc.congestionLevel cl " +
//        "WHERE f.member = :member")
//    List<Favorite> findByMemberWithJoin(@Param("member") Member member);


}
