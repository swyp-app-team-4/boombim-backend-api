package boombimapi.global.infra.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CorsProbeFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res,
        FilterChain chain) throws java.io.IOException, ServletException {

        String scheme = req.getScheme();
        String host   = req.getServerName();
        int    port   = req.getServerPort();

        String origin = req.getHeader("Origin");
        String xfh    = req.getHeader("X-Forwarded-Host");
        String xfp    = req.getHeader("X-Forwarded-Proto");
        String xfpPort= req.getHeader("X-Forwarded-Port");

        log.info("[CORS-PROBE] origin={}, scheme={}, host={}, port={}, url={}, xfh={}, xfp={}, xfpPort={}",
            origin, scheme, host, port, req.getRequestURL(), xfh, xfp, xfpPort);

        chain.doFilter(req, res);
    }
}