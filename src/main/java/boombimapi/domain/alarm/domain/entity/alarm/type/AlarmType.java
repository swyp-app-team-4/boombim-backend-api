package boombimapi.domain.alarm.domain.entity.alarm.type;


public enum AlarmType {
    ANNOUNCEMENT("공지사항"),
    COMMUNICATION("소통방"),
    VOTE("투표 종료 알림"),
    EVENT("이벤트");

    private final String description;

    AlarmType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
