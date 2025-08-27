package boombimapi.domain.member.presentation.dto.member.res;

public record ProfileRes(
        String profile
) {
    public static ProfileRes of(String profile){
        return new ProfileRes(profile);
    }
}
