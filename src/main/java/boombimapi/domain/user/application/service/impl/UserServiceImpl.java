package boombimapi.domain.user.application.service.impl;


import boombimapi.domain.user.application.service.UserService;
import boombimapi.domain.user.domain.entity.User;
import boombimapi.domain.user.domain.repository.UserRepository;
import boombimapi.domain.user.presentation.dto.req.NicknameReq;
import boombimapi.domain.user.presentation.dto.res.GetUserRes;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void updateNickname(String userId, NicknameReq req) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        user.updateName(req.nickname());
    }

    @Override
    public GetUserRes getUser(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new BoombimException(ErrorCode.USER_NOT_EXIST);

        return GetUserRes.of(user);
    }


}
