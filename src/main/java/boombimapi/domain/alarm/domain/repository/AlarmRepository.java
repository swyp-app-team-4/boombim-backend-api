package boombimapi.domain.alarm.domain.repository;


import boombimapi.domain.alarm.domain.entity.alarm.Alarm;
import boombimapi.domain.alarm.domain.entity.alarm.type.AlarmStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {


    // 상태별 알림 조회
    List<Alarm> findByStatus(AlarmStatus status);

    // 전송 대기 중인 알림 조회
    @Query("SELECT a FROM Alarm a WHERE a.status = 'PENDING' ORDER BY a.createdAt ASC")
    List<Alarm> findPendingAlarms();

    // 특정 기간 내 알림 조회
    @Query("SELECT a FROM Alarm a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<Alarm> findAlarmsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}