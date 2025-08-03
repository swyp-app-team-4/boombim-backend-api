package boombim.domain.oauth2.infra.filter;

import boombim.global.infra.exception.error.BoombimException;
import boombim.global.infra.exception.error.ErrorCode;
import boombim.global.jwt.domain.entity.JsonWebToken;
import boombim.global.jwt.domain.repository.JsonWebTokenRepository;
import boombim.global.jwt.domain.repository.KakaoJsonWebTokenRepository;
import boombim.global.jwt.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class BoombimLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final JsonWebTokenRepository jsonWebTokenRepository;
    private final KakaoJsonWebTokenRepository KakaoJsonWebTokenRepository;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        if(!(requestURI.equals("/api/oauth2/logout") && request.getMethod().equals("POST"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtUtil.getRefreshTokenFromCookies(request);

        if(!jwtUtil.jwtVerify(refreshToken, "refresh")) {
            throw new BoombimException(ErrorCode.JWT_ERROR_TOKEN);
        }

        LogoutProcess(refreshToken, response);
    }

    private void LogoutProcess(String refreshToken, HttpServletResponse response) {
        JsonWebToken jsonWebToken = jsonWebTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new BoombimException(ErrorCode.REFRESH_TOKEN_NOT_EXIST));

        KakaoJsonWebTokenRepository.deleteById(jsonWebToken.getProviderId());
        jsonWebTokenRepository.delete(jsonWebToken);

        response.addHeader("Authorization", "Bearer ");

        ResponseCookie refreshTokeCookie = jwtUtil.invalidRefreshToken();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokeCookie.toString());

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
