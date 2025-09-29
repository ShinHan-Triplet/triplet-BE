package org.zerock.triplet.domain.gather.dto;

import lombok.*;

public class InviteDTO {

    @Getter @Setter
    @Builder
    @AllArgsConstructor @NoArgsConstructor
    public static class CreateByEmailRequest {
        private Long gatherId;
        private String email;
    }

    @Getter @Setter @Builder
    @AllArgsConstructor @NoArgsConstructor
    public static class Simple {
        private Long inviteId;
        private Long gatherId;
        private String gatherName;
        private Integer status;
    }
}
