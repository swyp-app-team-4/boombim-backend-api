package boombimapi.global.config;

import boombimapi.global.infra.filter.BoombimJWTFilter;
import boombimapi.global.infra.exception.auth.BoombimAuthExceptionFilter;
import boombimapi.global.infra.filter.CustomSecurityLogger;
import boombimapi.global.jwt.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity(debug = false)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;

    private final List<String> excludedUrls = Arrays.asList(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/favicon.ico",
            "/api/reissue",
            "/api/region",
            "/api/oauth2/login/**",     // 새로운 토큰 방식 로그인 포함
            "/api/oauth2/callback/**",  // 기존 콜백 방식 (테스트용)
            "/api/oauth2/logout",
            "/api/healthcheck", "/api/admin/**",
            "/actuator/prometheus", "/metrics"
    );

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors((cors) -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(List.of("http://localhost:3000", "https://boombim.netlify.app"));
                    config.setAllowedMethods(Collections.singletonList("*"));
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setExposedHeaders(Collections.singletonList("Authorization"));
                    config.setMaxAge(3600L);
                    return config;
                }))
                .authorizeHttpRequests((url) -> url
                        .requestMatchers("/api/healthcheck").permitAll()
                        .requestMatchers("/api/oauth2/login/**").permitAll()     // POST /api/oauth2/login/{provider} 허용
                        .requestMatchers("/api/oauth2/callback/**").permitAll()  // 기존 콜백 방식 허용
                        .requestMatchers("/api/oauth2/logout", "/api/admin/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/favicon.ico", "/api/region").permitAll()
                        .requestMatchers("/api/reissue").permitAll()
                        .requestMatchers("/actuator/prometheus","/metrics").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(except -> except
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                        ))

                .addFilterBefore(new CustomSecurityLogger(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new BoombimAuthExceptionFilter(objectMapper), CorsFilter.class)
                .addFilterAfter(new BoombimJWTFilter(jwtUtil, excludedUrls), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}