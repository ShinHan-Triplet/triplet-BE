package org.zerock.triplet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;


@Configuration
public class AwsConfig {
    @Value("${triplet.s3.region}") String region;
    @Value("${aws.accessKeyId}") String accessKey;
    @Value("${aws.secretAccessKey}") String secret;

    private AwsCredentialsProvider creds() {
        AwsCredentials base = AwsBasicCredentials.create(accessKey, secret);
        return StaticCredentialsProvider.create(base);
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(creds())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(creds())
                .build();
    }

//    @Bean
//    public S3Client s3Client(){
//        return S3Client.builder()
//                .region(Region.of(region))
//                .credentialsProvider(
//                        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secret))
//                )
//                .build();
//    }
}
