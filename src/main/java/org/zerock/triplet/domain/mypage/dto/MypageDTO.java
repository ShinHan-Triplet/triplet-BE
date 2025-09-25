package org.zerock.triplet.domain.mypage.dto;

import lombok.*;

import java.util.List;

public class MypageDTO {
    private MypageDTO() {}

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyInfoResponse {
        private String name;
        private String birthday;
        private String profileImage;
    }

    @Getter
    @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MyGatherSummary {
        private Long gatherId;
        private String gatherName;
        private boolean owner;
        private List<String> members;
        private String cardNickname;
    }
}
