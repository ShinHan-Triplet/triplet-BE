package org.zerock.triplet.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.member.repository.MemberRepository;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException{
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(StringUtils.hasText(auth) && auth.startsWith("Bearer ")){
            String token = auth.substring(7);
            try{
                Claims claims = jwtUtil.validateAndGetClaims(token);
                Long memberId = Long.valueOf(claims.getSubject());
                Member member = memberRepository.findById(memberId).orElse(null);
                if(member != null){
                    var authorities = List.of(new SimpleGrantedAuthority(member.getRole()));
                    var authToken = new UsernamePasswordAuthenticationToken(member, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }catch (Exception ignored){}
        }
        chain.doFilter(request, response);
    }
}
