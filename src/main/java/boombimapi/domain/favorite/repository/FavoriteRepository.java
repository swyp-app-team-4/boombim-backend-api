package boombimapi.domain.favorite.repository;

import boombimapi.domain.favorite.entity.Favorite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

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

}
