package org.zerock.triplet.security.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.member.repository.MemberRepository;
import org.zerock.triplet.security.JWTUtil;
import org.zerock.triplet.security.RefreshToken;
import org.zerock.triplet.security.RefreshTokenRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.oauth2.redirect-success}")
    private String redirectUrl;

    @Value("${jwt.refresh.exp.seconds}")
    private long refreshExpSeconds;

    @Override
    public void onAuthenticationSuccess(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException{
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String oauthId = (String) principal.getAttributes().get("id");

        Member member = memberRepository.findByOauthId(oauthId).orElseThrow();

        // Access/Refresh 발급
        String access = jwtUtil.createAccessToken(
                member.getId(),
                Map.of(
                        "email", member.getEmail(),
                        "name", member.getName(),
                        "role", member.getRole()
                )
        );
        String refresh = jwtUtil.createRefreshToken(member.getId());

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .memberId(member.getId())
                        .token(refresh)
                        .expiresAt(Instant.now().plusSeconds(refreshExpSeconds))
                        .build()
        );

        Cookie refreshCookie = new Cookie("refresh_token", refresh);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int)refreshExpSeconds);
        response.addCookie(refreshCookie);

        response.sendRedirect(redirectUrl);
    }
}
