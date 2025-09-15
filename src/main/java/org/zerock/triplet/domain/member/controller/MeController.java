package org.zerock.triplet.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zerock.triplet.domain.member.entity.Member;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Member m) {
        // (참고) 우리 보안 설정상 인증이 없으면 컨트롤러까지 오기 전에 401이 나가므로
        // 이 분기는 거의 안 타지만, 혹시 모를 경우 대비해 둡니다.
        if (m == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
        }

        Map<String,Object> attrs = new LinkedHashMap<>();
        attrs.put("id", m.getId());
        attrs.put("email", m.getEmail());            // null OK
        attrs.put("name", m.getName());              // null OK
        attrs.put("birthday", m.getBirthday());      // null OK
        attrs.put("birthyear", null);                // null OK
        attrs.put("profile_image", m.getProfileImage()); // null OK

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "attrs", attrs
        ));
    }
}
