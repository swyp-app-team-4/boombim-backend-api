package boombimapi.domain.alarm.domain.repository;

import boombimapi.domain.alarm.domain.entity.alarm.AlarmRecipient;
import boombimapi.domain.alarm.domain.entity.fcm.type.DeviceType;

import boombimapi.domain.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRecipientRepository extends JpaRepository<AlarmRecipient, Long> {
    List<AlarmRecipient> findAllByMemberAndDeviceTypeOrderByCreatedAtAsc(
            Member user, DeviceType deviceType);
}
