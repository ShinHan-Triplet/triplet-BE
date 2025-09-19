package org.zerock.triplet.domain.trip.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageStorage {
    // 파일을 저장하고 db에 저장할 key/filename 리턴
    String store(MultipartFile file) throws IOException;
    
    // 화면에 뿌릴 절대/상대 url 생성
    String publicUrl(String key);
}
