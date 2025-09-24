package org.zerock.triplet.domain.draft;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zerock.triplet.domain.member.entity.Member;

import java.util.Map;

@RestController
@RequestMapping("/api/drafts/me")
@RequiredArgsConstructor
public class DraftController {
    private final DraftService drafts;
    private final S3CoverService s3;

    @GetMapping
    public Object get(@AuthenticationPrincipal Member member){
        long userId = member.getId();
        return drafts.get(userId)
                .<Object>map(d -> Map.of("hasDraft", true, "draft", d))
                .orElse(Map.of("hasDraft", false));
    }

    @PutMapping
    public Draft upsert(@AuthenticationPrincipal Member member,
                        @RequestBody Map<String, Object> patch){
        return drafts.upsertMerge(member.getId(), patch);
    }

    @DeleteMapping
    public void deleteDraft(@AuthenticationPrincipal Member member){
        drafts.delete(member.getId());
    }

    @PostMapping("/cover/presign")
    public Object presign(@AuthenticationPrincipal Member member,
                          @RequestBody PresignReq req){
        var r = s3.presignForTmp(member.getId(), req.filename, req.contentType);
        return Map.of("objectKey", r.objectKey(), "uploadUrl", r.uploadUrl(), "viewUrl", r.viewUrl());
    }
    @Data static class PresignReq{String filename; String contentType;}

    @PostMapping("/cover/confirm")
    public Draft confirm(@AuthenticationPrincipal Member member,
                         @RequestBody Draft.Cover cover){
        return drafts.upsertMerge(member.getId(), Map.of("cover", cover));
    }


}
