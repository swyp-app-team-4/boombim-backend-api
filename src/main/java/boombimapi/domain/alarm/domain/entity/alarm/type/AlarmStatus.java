package boombimapi.domain.alarm.domain.entity.alarm.type;

public enum AlarmStatus {
    PENDING("전송 대기"),
    QUEUED("큐잉 완료"),
    SENDING("전송 중"),
    SENT("전송 완료"),
    FAILED("전송 실패");

    private final String description;

    AlarmStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}