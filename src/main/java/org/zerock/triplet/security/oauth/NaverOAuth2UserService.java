package org.zerock.triplet.security.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.member.repository.MemberRepository;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NaverOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    private static String toYyyyDotMmDotDd(String birthyear, String mmdd) {
        if (birthyear == null || mmdd == null) return null;
        String[] p = mmdd.split("-");
        if (p.length != 2) return null;
        try {
            int m = Integer.parseInt(p[0]);
            int d = Integer.parseInt(p[1]);
            return String.format("%s. %02d. %02d", birthyear, m, d); // "YYYY. MM. DD"
        } catch (NumberFormatException e) {
            return null;
        }
    }


    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException{
        OAuth2User user = super.loadUser(req);

        Map<String, Object> attr = user.getAttributes();
        @SuppressWarnings("unchecked")
        Map<String, Object> res = (Map<String, Object>) attr.get("response");

        // 네이버가 주는 key들 (동의 안 했거나, 항목 미설정이면 null일 수 있음)
        String naverId = (String) res.get("id");
        String email   = (String) res.get("email");
        String name    = (String) res.get("name");
        String mmdd         = (String) res.get("birthday");     // "MM-DD"
        String year         = (String) res.get("birthyear");    // "YYYY"
        String profileImage = (String) res.get("profile_image"); // URL

        if(naverId == null){
            throw new OAuth2AuthenticationException("NAVER response does not include 'id'");
        }

        String formattedBirthday = toYyyyDotMmDotDd(year, mmdd); // "YYYY. MM. DD" or null

        Member member = memberRepository.findByOauthId(naverId)
                .map(m -> {
                    m.setEmail(email);
                    m.setName(name);
                    m.setBirthday(formattedBirthday);
                    m.setProfileImage(profileImage);
                    return m;
                })
                .orElseGet(() -> Member.builder()
                        .oauthId(naverId)
                        .email(email)
                        .name(name)
                        .birthday(formattedBirthday)
                        .profileImage(profileImage)
                        .role("ROLE_USER")
                        .build()
                );
        memberRepository.save(member);

        Map<String, Object> principalAttrs = Map.of(
                "id", naverId,
                "email", email,
                "name", name,
                "birthday", formattedBirthday,
                "birthyear", year,
                "profile_image", profileImage
        );

        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority(member.getRole())),
                principalAttrs,
                "id"
        );
    }
}
