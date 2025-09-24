package org.zerock.triplet.domain.trip.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.triplet.domain.draft.Draft;
import org.zerock.triplet.domain.draft.DraftService;
import org.zerock.triplet.domain.draft.S3CoverService;
import org.zerock.triplet.domain.gather.service.GatherService;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.trip.dto.PublishReq;
import org.zerock.triplet.domain.trip.dto.PublishResult;
import org.zerock.triplet.domain.trip.entity.Trip;
import org.zerock.triplet.domain.trip.service.TripPublishService;

import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripPublishController {

    private final TripPublishService tripPublishService;

    @PostMapping
    public PublishResult publish(@AuthenticationPrincipal Member me,
                                 @RequestBody PublishReq req){
        return tripPublishService.publish(me, req);
    }

    @PostMapping("/from-draft")
    public PublishResult publishFromDraft(
            @AuthenticationPrincipal Member me,
            @RequestBody PublishReq req
    ){
        try{
            return tripPublishService.publish(me, req);
        }catch(IllegalArgumentException e){
            // 유효성 오류 (요청 값 잘못됨, 모임 없음 등)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }catch(SecurityException e){
            // 모임 권한 없음
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        }catch (IllegalStateException e){
            // 초안 없음 등 상태 오류
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @Data
    static class CreateFromDraftReq {
        private Long gatherId;
        private boolean solo;
    }
}
