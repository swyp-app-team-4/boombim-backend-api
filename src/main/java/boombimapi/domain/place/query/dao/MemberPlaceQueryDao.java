package boombimapi.domain.place.query.dao;

import boombimapi.domain.place.query.dao.param.ViewportParam;
import boombimapi.domain.place.query.dao.row.MemberPlaceViewportRow;
import java.util.List;

public interface MemberPlaceQueryDao {

    // TODO: 이거 필요할지 생각해봐야 함
    List<MemberPlaceViewportRow> findInViewPort(
        ViewportParam memberplaceviewportParam
    );

}
