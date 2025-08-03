package boombim.global.jwt.domain.repository;


import boombim.domain.oauth2.domain.entity.SocialProvider;
import boombim.global.jwt.domain.entity.SocialToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialTokenRepository extends CrudRepository<SocialToken, String> {

    Optional<SocialToken> findByUserIdAndProvider(String userId, SocialProvider provider);

    void deleteByUserIdAndProvider(String userId, SocialProvider provider);

    void deleteByUserId(String userId);
}