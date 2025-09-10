package boombimapi.domain.vote.domain.entity.type;

public enum VoteAnswerType {
    RELAXED("여유"),
    COMMONLY("보통"),
    BUSY("약간 붐빔"),
    CROWDED("붐빔");

    private final String displayName;

    VoteAnswerType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
