package boombimapi.global.infra.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class CustomSecurityLogger extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {
        String uri = request.getRequestURI();

        // prometheus 요청은 제외
// prometheus 요청과 metrics 요청은 제외
        if (!uri.startsWith("/actuator/prometheus") && !uri.startsWith("/metrics")) {
            log.info("Security Request: {} {}", request.getMethod(), uri);
        }


        filterChain.doFilter(request, response);
    }
}
