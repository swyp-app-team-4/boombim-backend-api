package boombimapi.domain.search.presentation.dto.res;

public record SearchHistoryRes(
        Long searchId,

        String posName
) {
    public static SearchHistoryRes of(Long searchId, String posName){
        return new SearchHistoryRes(searchId, posName);
    }
}
