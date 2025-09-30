package org.zerock.triplet.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.zerock.triplet.domain.member.repository.MemberRepository;
import org.zerock.triplet.security.JWTUtil;
import org.zerock.triplet.security.JwtAuthenticationFilter;
import org.zerock.triplet.security.oauth.NaverOAuth2UserService;
import org.zerock.triplet.security.oauth.OAuth2SuccessHandler;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final NaverOAuth2UserService naverOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    /** 공통 CORS */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowCredentials(true);
        c.setAllowedOrigins(List.of("http://localhost:3000","http://localhost:5173"));
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        c.setExposedHeaders(List.of("Authorization","Content-Type"));
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }

    /** 1) API 체인: /api/** 전용 — 실패 시 401 고정 (리다이렉트 금지) */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                // 컨텍스트패스 있어도 /api/** 만 정확히 매칭
                .securityMatcher((HttpServletRequest req) -> {
                    String uri = req.getRequestURI();                 // /api/me 또는 /triplet/api/me
                    String ctx = req.getContextPath();                // "" 또는 "/triplet"
                    String path = (ctx == null || ctx.isEmpty()) ? uri : uri.substring(ctx.length());
                    return path.startsWith("/api/");
                })
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/drafts/**").authenticated()
                        .requestMatchers("/api/trips/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/mytrip/**").permitAll()
                        .requestMatchers("/api/mytrip/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/card/list").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // ★ 무조건 401
                        .accessDeniedHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
                )
                .requestCache(c -> c.disable());

        // JWT 필터 (API 체인에만)
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil, memberRepository),
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    /** 2) 웹 체인: 나머지 — OAuth2 로그인(네이버) */
    @Bean
    @Order(2) // 명시적으로 두 번째
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**", "/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(u -> u.userService(naverOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                // ★ 혹시라도 /api/**가 여기로 새어오면 401로만 응답하게 (2중 안전장치)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
                )
                .requestCache(c -> c.disable());

        return http.build();
    }
}
