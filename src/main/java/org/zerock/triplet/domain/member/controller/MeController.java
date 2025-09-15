package org.zerock.triplet.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {

    @GetMapping("/me")
    public Object me(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) return Map.of("authenticated", false);
        return Map.of("authenticated", true, "attrs", user.getAttributes());
    }
}
