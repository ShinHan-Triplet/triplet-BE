package org.zerock.triplet.domain.mytrip.dto;

import lombok.Data;

@Data
public class PresignReq {
    private String filename;
    private String contentType;
}
