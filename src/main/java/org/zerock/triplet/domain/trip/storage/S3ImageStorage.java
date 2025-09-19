package org.zerock.triplet.domain.trip.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ImageStorage implements ImageStorage{

    private final S3Client s3;
    @Value("${app.s3.bucket}") String bucket;
    @Value("${app.s3.folder:trips/}") String folder;
    @Value("${app.upload.public-base}") String publicBase;

    @Override
    public String store(MultipartFile file) throws IOException{
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(fn -> fn.contains("."))
                .map(fn -> fn.substring(fn.lastIndexOf('.')))
                .orElse(".bin");
        if (!List.of(".jpg",".jpeg",".png",".webp",".gif").contains(ext.toLowerCase()))
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");

        String key = folder + "trip_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + ext;

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket).key(key)
                .contentType(Optional.ofNullable(file.getContentType())
                        .orElse("application/octet-stream"))
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();
        s3.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return key;
    }

    @Override
    public String publicUrl(String key) {
        // S3 퍼블릭 URL 프리픽스 사용
        String fileName = key.startsWith(folder) ? key.substring(folder.length()) : key;
        return publicBase + fileName;
    }
}
