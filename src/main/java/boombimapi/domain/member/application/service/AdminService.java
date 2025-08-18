package boombimapi.domain.member.application.service;

import boombimapi.domain.member.presentation.dto.admin.req.AdminLoginReq;
import boombimapi.domain.oauth2.presentation.dto.res.LoginToken;

public interface AdminService {
    LoginToken postLogin(AdminLoginReq req);
}