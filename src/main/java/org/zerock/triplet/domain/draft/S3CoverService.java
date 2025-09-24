package org.zerock.triplet.domain.draft;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3CoverService {

    private final S3Presigner presigner;
    private final S3Client s3;

    @Value("${triplet.s3.bucket}") private String bucket;
    @Value("${triplet.s3.region}") private String region;
    @Value("${app.s3.tmpPrefix}") private String tmpPrefix;
    @Value("${app.s3.publicPrefix}") private String publicPrefix;

    public PresignResult presignForTmp(long userId, String filename, String contentType){
        String ext = filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.'))
                : ".jpg";
        String key = "%s/%d/cover/%s%s".formatted(tmpPrefix, userId, UUID.randomUUID(), ext);

        String ct = (contentType == null || contentType.isBlank())
                ? "application/octet-stream" : contentType;

        var put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(ct)
                .build();

        var preq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(put)
                .build();

        var presigned = presigner.presignPutObject(preq);

        // 보기 URL은 SDK 유틸리티로 생성 (리전/버킷 규칙 자동 처리)
        String viewUrl = s3.utilities()
                .getUrl(b -> b.bucket(bucket).key(key).region(Region.of(region)))
                .toString();

        return new PresignResult(key, presigned.url().toString(), viewUrl);
    }

    public String promoteToFinal(long tripId, String tmpKey){
        String fileName = tmpKey.substring(tmpKey.lastIndexOf('/') + 1);
        String finalKey = String.format("%s/%d/cover/%s", publicPrefix, tripId, fileName);

        s3.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucket).sourceKey(tmpKey)
                .destinationBucket(bucket).destinationKey(finalKey)
                .build());

        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket).key(tmpKey).build());

        return finalKey;
    }

    public record PresignResult(String objectKey, String uploadUrl, String viewUrl){}
}
