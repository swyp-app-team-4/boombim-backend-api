package boombimapi.domain.region.application.service;

import boombimapi.domain.region.presentation.dto.res.RegionRes;

import java.time.LocalDate;
import java.util.List;

public interface RegionService {

    List<RegionRes> getRegion(LocalDate date);
}
