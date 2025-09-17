package boombimapi.domain.place.query.dao;

import boombimapi.domain.place.query.dao.param.OfficialPlaceViewportParam;
import boombimapi.domain.place.query.dao.row.OfficialPlaceViewportRow;
import java.util.List;

public interface OfficialPlaceQueryDao {

    List<OfficialPlaceViewportRow> findInViewport(
        OfficialPlaceViewportParam officialPlaceViewportParam
    );

}
