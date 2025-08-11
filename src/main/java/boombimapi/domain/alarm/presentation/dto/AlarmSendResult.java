package boombimapi.domain.alarm.presentation.dto;

import java.util.List;

/**
 * 알림 전송 결과 DTO
 */
public record AlarmSendResult(
        int successCount,
        int failureCount,
        List<String> invalidTokens
) {}
