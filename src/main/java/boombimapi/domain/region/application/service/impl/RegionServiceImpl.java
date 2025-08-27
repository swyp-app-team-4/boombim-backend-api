package boombimapi.domain.region.application.service.impl;

import boombimapi.domain.region.application.service.RegionService;
import boombimapi.domain.region.domain.entity.Region;
import boombimapi.domain.region.domain.repository.RegionRepository;
import boombimapi.domain.region.presentation.dto.res.RegionRes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RegionServiceImpl implements RegionService {
    private final RegionRepository regionRepository;

    @Override
    public List<RegionRes> getRegion(LocalDate date) {
        List<Region> findDate = regionRepository.findAllByRegionDate(date);

        List<RegionRes> result = new ArrayList<>();

        for (Region region : findDate) {
            result.add(RegionRes.of(region.getRegionDate(), region.getStartTime(),
                    region.getEndTime(), region.getPosName(), region.getPeopleCnt()));
        }

        return result;
    }
}
