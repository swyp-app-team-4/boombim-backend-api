package boombimapi.domain.search.presentation.dto.res;

public record SearchRelatedRes(
        String posName
) {
    public static SearchRelatedRes of(String posName){
        return new SearchRelatedRes(posName);
    }
}
