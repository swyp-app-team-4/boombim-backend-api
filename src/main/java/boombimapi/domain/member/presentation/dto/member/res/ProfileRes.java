package boombimapi.domain.member.presentation.dto.member.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 사진 변경된거")
public record ProfileRes(
        String profile
) {
    public static ProfileRes of(String profile){
        return new ProfileRes(profile);
    }
}
