package org.zerock.triplet.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@Log4j2
public class JWTUtil {
    @Value("${jwt.secret.key}")
    private String secret;

    @Value("${jwt.access.exp.seconds:900}")      // 15분 기본
    private long accessExpSeconds;

    @Value("${jwt.refresh.exp.seconds:1209600}") // 14일 기본
    private long refreshExpSeconds;

    private SecretKey key;
    private final MacAlgorithm alg = Jwts.SIG.HS256;

    @PostConstruct
    void init(){
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId, Map<String, Object> extraClaims){
        Instant now = Instant.now();
        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpSeconds)));

        if(extraClaims != null && !extraClaims.isEmpty()){
            extraClaims.forEach(builder::claim);
        }
        return builder.signWith(key, alg).compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpSeconds)))
                .signWith(key, alg)
                .compact();
    }

    public Claims validateAndGetClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

}
