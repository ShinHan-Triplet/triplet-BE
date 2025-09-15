package org.zerock.triplet.security.oauth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.member.repository.MemberRepository;
import org.zerock.triplet.security.JWTUtil;
import org.zerock.triplet.security.RefreshToken;
import org.zerock.triplet.security.RefreshTokenRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(
//        origins = {"http://localhost:3000","http://localhost:5173"},
//        allowCredentials = "true",
//        allowedHeaders = {"Authorization","Content-Type","X-Requested-With"},
//        methods = {RequestMethod.POST, RequestMethod.OPTIONS}
//)
@RequiredArgsConstructor
public class AuthController {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return ResponseEntity.status(401).body("no cookie");

        String refresh = Arrays.stream(cookies)
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);

        if(refresh == null) return ResponseEntity.status(401).body("no refresh token");

        Claims claims;
        try{
            claims = jwtUtil.validateAndGetClaims(refresh);
            if(!"refresh".equals(claims.get("type", String.class))){
                return ResponseEntity.status(401).body("not refresh token");
            }
        }catch (Exception e){
            return ResponseEntity.status(401).body("invalid token");
        }

        Long userId = Long.valueOf(claims.getSubject());
        RefreshToken saved = refreshTokenRepository.findById(userId).orElse(null);
        if(saved == null || !saved.getToken().equals(refresh) || saved.getExpiresAt().isBefore(Instant.now())){
            return ResponseEntity.status(401).body("refresh expired or mismatched");
        }

        Member member = memberRepository.findById(userId).orElseThrow();
        String newAccess = jwtUtil.createAccessToken(
                member.getId(),
                Map.of("email", member.getEmail(), "name", member.getName(), "role", member.getRole())
        );
        return ResponseEntity.ok(newAccess);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response){
        Cookie clear = new Cookie("refresh_token", "");
        clear.setHttpOnly(true);
        clear.setSecure(true);
        clear.setPath("/");
        clear.setMaxAge(0);
        response.addCookie(clear);

        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            Arrays.stream(cookies)
                    .filter(c -> "refresh_token".equals(c.getName()))
                    .findFirst()
                    .ifPresent(c -> {
                        try{
                            Long uid = Long.valueOf(jwtUtil.validateAndGetClaims(c.getValue()).getSubject());
                            refreshTokenRepository.deleteById(uid);
                        }catch(Exception ignored){}
                    });
        }
        return ResponseEntity.ok().build();
    }
}
