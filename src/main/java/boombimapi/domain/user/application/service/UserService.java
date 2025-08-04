package boombimapi.domain.user.application.service;


import boombimapi.domain.user.presentation.dto.res.GetUserRes;

public interface UserService {

    GetUserRes getUser(String userId);

}