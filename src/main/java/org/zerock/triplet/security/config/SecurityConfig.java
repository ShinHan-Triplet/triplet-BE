package org.zerock.triplet.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.zerock.triplet.domain.member.service.NaverOAuth2UserService;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final NaverOAuth2UserService naverOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error",
                                "/oauth2/**", "/login/**",
                                "/api/me"              // ★ 로그인 상태 확인은 공개
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(o -> o
                        .userInfoEndpoint(ui -> ui.userService(naverOAuth2UserService))
                        .successHandler((req, res, auth) -> {
                            // 로그인 성공 후 프론트로 돌려보내기
//                            res.sendRedirect("http://localhost:3000");
                            res.sendRedirect("http://localhost:3000/auth/callback");
                        })
                        .failureHandler((req, res, ex) -> {
                            res.sendRedirect("http://localhost:3000/login?error=naver");
                        })
                )
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/"))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
//        config.setAllowedOrigins(List.of("http://localhost:3000","http://127.0.0.1:3000"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
