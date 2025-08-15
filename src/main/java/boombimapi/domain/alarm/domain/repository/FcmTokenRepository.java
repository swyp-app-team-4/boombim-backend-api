package boombimapi.domain.alarm.domain.repository;

import boombimapi.domain.alarm.domain.entity.fcm.FcmToken;
import boombimapi.domain.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    // 사용자별 활성화된 토큰 조회
    List<FcmToken> findByMemberIdAndIsActiveTrue(String userId);

    // 모든 활성화된 토큰 조회 (전체 알림용) - alarmFlag가 false인 유저만
    @Query("SELECT f FROM FcmToken f WHERE f.isActive = true AND f.member.alarmFlag = false")
    List<FcmToken> findAllActiveTokens();

    // 투표 종료 유저 알림용 - alarmFlag가 false인 유저만
    @Query("SELECT f FROM FcmToken f " +
            "WHERE f.member IN :users " +
            "AND f.isActive = true " +
            "AND f.member.alarmFlag = false " +
            "ORDER BY f.member.id ASC, f.lastUsedAt DESC")
    List<FcmToken> findActiveTokensForUsers(@Param("users") List<Member> users);


    // 토큰으로 조회
    Optional<FcmToken> findByToken(String token);

    // 사용자와 토큰으로 조회
    Optional<FcmToken> findByMemberIdAndToken(String userId, String token);

    // 비활성화된 토큰 삭제
    @Modifying
    @Query("DELETE FROM FcmToken f WHERE f.isActive = false AND f.lastUsedAt < :cutoffDate")
    void deleteInactiveTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 사용자의 기존 토큰들 비활성화
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false WHERE f.member.id = :userId AND f.token != :currentToken")
    void deactivateOtherTokens(@Param("userId") String userId, @Param("currentToken") String currentToken);
}
