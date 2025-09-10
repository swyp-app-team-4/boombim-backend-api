package boombimapi.domain.clova.dto.request;

import java.util.List;

public record ChatMessage(
    String role,
    List<ChatContent> content
) {

}
