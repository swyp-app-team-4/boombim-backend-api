package boombimapi.domain.alarm.domain.entity.alarm.type;


public enum AlarmType {
    ANNOUNCEMENT("공지사항"),
    PROMOTION("프로모션"),
    SYSTEM("시스템 알림"),
    EVENT("이벤트");

    private final String description;

    AlarmType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
