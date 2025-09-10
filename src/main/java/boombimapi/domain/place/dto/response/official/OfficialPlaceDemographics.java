package boombimapi.domain.place.dto.response.official;

import boombimapi.domain.congestion.entity.OfficialCongestionDemographics;

// TODO: 여기 열거형으로 처리하기
public record OfficialPlaceDemographics(
    String category,
    String subCategory,
    Double rate
) {

    public static OfficialPlaceDemographics from(
        OfficialCongestionDemographics entity
    ) {
        return new OfficialPlaceDemographics(
            entity.getCategory(),
            entity.getSubCategory(),
            entity.getRate()
        );
    }

}
