package boombimapi.domain.favorite.repository;

import boombimapi.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByMemberIdAndMemberPlaceId(
        String memberId,
        Long memberPlaceId
    );

    int deleteByMemberIdAndMemberPlaceId(
        String memberId,
        Long memberPlaceId
    );

}
