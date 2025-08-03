package boombim.domain.user.application.service;


import boombim.domain.user.presentation.dto.req.NicknameReq;
import boombim.domain.user.presentation.dto.res.GetUserRes;

public interface UserService {

    void updateNickname(String userId, NicknameReq req);

    GetUserRes getUser(String userId);

}