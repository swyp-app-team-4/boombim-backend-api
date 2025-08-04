package boombimapi.domain.user.application.service;


import boombimapi.domain.user.presentation.dto.req.NicknameReq;
import boombimapi.domain.user.presentation.dto.res.GetUserRes;

public interface UserService {

    void updateNickname(String userId, NicknameReq req);

    GetUserRes getUser(String userId);

}