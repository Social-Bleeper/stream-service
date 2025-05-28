package com.bleeper.stream_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class SupabaseS3Config {

    @Value("${supabase.access-key}")
    private String accessKey;

    @Value("${supabase.secret-key}")
    private String secretKey;

    @Value("${supabase.project-ref}")
    private String projectRef;

    @Bean
    public S3Client supabaseS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create("https://" + projectRef + ".supabase.co/storage/v1/s3"))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .region(Region.US_EAST_1)        // Region must be set, but is not used
                .forcePathStyle(true)   // IMPORTANT: Supabase needs path-style access
                .build();
    }
}
