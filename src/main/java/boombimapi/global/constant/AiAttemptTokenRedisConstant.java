package boombimapi.global.constant;

public final class AiAttemptTokenRedisConstant {

    private AiAttemptTokenRedisConstant() {
    }

    public static final String KEY_PREFIX_AI_ATTEMPT_META = "ai:attempt:";
    public static final String KEY_PREFIX_ACTIVE_POINTER = "ai:activeAttempt:";
    public static final String KEY_PREFIX_USED_FLAG = "ai:attempt:used:";

    public static final String FIELD_MEMBER_ID = "memberId";
    public static final String FIELD_MEMBER_PLACE_ID = "memberPlaceId";
    public static final String FIELD_CREATED_AT = "createdAt";

}
