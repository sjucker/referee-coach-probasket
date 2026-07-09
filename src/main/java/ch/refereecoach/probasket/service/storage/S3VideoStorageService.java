package ch.refereecoach.probasket.service.storage;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
public class S3VideoStorageService implements VideoStorageService {

    private final ApplicationProperties.Storage config;
    private final S3Client s3Client;
    private final S3Presigner presigner;

    public S3VideoStorageService(ApplicationProperties applicationProperties) {
        this.config = applicationProperties.getStorage();

        var credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey()));
        var region = Region.of(config.getRegion());

        // path-style access is required by MinIO and works with Cloudflare R2; AWS S3 uses virtual-hosted style
        var usePathStyle = isNotBlank(config.getEndpoint());
        var serviceConfiguration = S3Configuration.builder()
                                                   .pathStyleAccessEnabled(usePathStyle)
                                                   .build();

        var clientBuilder = S3Client.builder()
                                    .region(region)
                                    .credentialsProvider(credentials)
                                    .serviceConfiguration(serviceConfiguration);
        var presignerBuilder = S3Presigner.builder()
                                          .region(region)
                                          .credentialsProvider(credentials)
                                          .serviceConfiguration(serviceConfiguration);
        if (usePathStyle) {
            var endpoint = URI.create(config.getEndpoint());
            clientBuilder.endpointOverride(endpoint);
            presignerBuilder.endpointOverride(endpoint);
        }

        this.s3Client = clientBuilder.build();
        this.presigner = presignerBuilder.build();
    }

    @Override
    public String createUploadUrl(String objectKey, String contentType, Duration ttl) {
        var objectRequest = PutObjectRequest.builder()
                                            .bucket(config.getBucket())
                                            .key(objectKey)
                                            .contentType(contentType)
                                            .build();
        var presignRequest = PutObjectPresignRequest.builder()
                                                    .signatureDuration(ttl)
                                                    .putObjectRequest(objectRequest)
                                                    .build();
        return presigner.presignPutObject(presignRequest).url().toString();
    }

    @Override
    public String createDownloadUrl(String objectKey, Duration ttl) {
        var objectRequest = GetObjectRequest.builder()
                                            .bucket(config.getBucket())
                                            .key(objectKey)
                                            .build();
        var presignRequest = GetObjectPresignRequest.builder()
                                                    .signatureDuration(ttl)
                                                    .getObjectRequest(objectRequest)
                                                    .build();
        return presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                                                 .bucket(config.getBucket())
                                                 .key(objectKey)
                                                 .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public void delete(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                                                 .bucket(config.getBucket())
                                                 .key(objectKey)
                                                 .build());
    }

    @PreDestroy
    void close() {
        s3Client.close();
        presigner.close();
    }
}
