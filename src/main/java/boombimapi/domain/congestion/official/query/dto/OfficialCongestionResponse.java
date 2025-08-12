package boombimapi.domain.congestion.official.query.dto;


import java.time.LocalDateTime;

/**
 *
 * 조회(Query) 전용 DTO*
 * <p>QueryDSL constructor projection으로 매핑</p>
 *
 * @param officialCongestionId 공식 혼잡도 ID
 * @param officialPlaceId      공식 장소 ID
 * @param officialPlaceName    공식 장소 장소명
 * @param congestionLevelId    혼잡도 수준 ID
 * @param congestionLevelName  혼잡도 수준 이름
 * @param congestionMessage    혼잡도 수준 메시지
 * @param populationMin        최소 인구 수
 * @param populationMax        최대 인구 수
 * @param observedAt           관측 시각
 */
public record OfficialCongestionResponse(
    Long officialCongestionId,
    Long officialPlaceId,
    String officialPlaceName,
    Integer congestionLevelId,
    String congestionLevelName,
    String congestionMessage,
    Long populationMin,
    Long populationMax,
    LocalDateTime observedAt
) {

}
