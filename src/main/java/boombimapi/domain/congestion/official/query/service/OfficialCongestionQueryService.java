package boombimapi.domain.congestion.official.query.service;

import static boombimapi.global.infra.exception.error.ErrorCode.*;

import boombimapi.domain.congestion.official.query.dto.OfficialCongestionResponse;
import boombimapi.domain.congestion.official.query.repository.OfficialCongestionQueryRepository;
import boombimapi.global.infra.exception.error.BoombimException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfficialCongestionQueryService {

    private final OfficialCongestionQueryRepository officialCongestionQueryRepository;

    public OfficialCongestionResponse getLatestOfficialCongestion(
        Long officialPlaceId
    ) {
        return officialCongestionQueryRepository
            .findLatestOfficialCongestionByOfficialPlaceId(officialPlaceId)
            .orElseThrow(() -> new BoombimException(OFFICIAL_CONGESTION_NOT_FOUND));
    }

}
