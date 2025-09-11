package boombimapi.global.infra.exception.auth;


import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import boombimapi.global.infra.exception.error.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class BoombimAuthExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BoombimException e) {
            handleFlowException(response, e);
        } catch (AuthenticationException e) {
            handleAuthenticationException(response);
        } catch (Exception e) {
            log.error("Filter에서 예상치 못한 오류 발생", e);
            handleUnexpectedException(response);
        }
    }

    private void handleFlowException(HttpServletResponse response, BoombimException e) throws IOException {
        log.error("Filter에서 BoombimException 발생 - ErrorCode: {}, Message: {}",
                e.getErrorCode(), e.getMessage());

        response.setStatus(e.getHttpStatusCode());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(e);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private void handleAuthenticationException(HttpServletResponse response) throws IOException {
        log.error("Filter에서 AuthenticationException 발생");

        response.setStatus(401);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private void handleUnexpectedException(HttpServletResponse response) throws IOException {
        response.setStatus(500);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.SERVER_UNTRACKED_ERROR);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
