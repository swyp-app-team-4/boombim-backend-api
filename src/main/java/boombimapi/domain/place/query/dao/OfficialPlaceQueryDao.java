package boombimapi.domain.place.query.dao;

import boombimapi.domain.place.query.dao.param.ViewportParam;
import boombimapi.domain.place.query.dao.row.OfficialPlaceViewportRow;
import java.util.List;

public interface OfficialPlaceQueryDao {

    List<OfficialPlaceViewportRow> findInViewport(
        ViewportParam viewportParam
    );

}
