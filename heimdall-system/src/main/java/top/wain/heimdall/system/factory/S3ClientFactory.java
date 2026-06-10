package top.wain.heimdall.system.factory;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.utils.SdkAutoCloseable;
import top.wain.heimdall.system.model.entity.StorageDO;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步 S3 客户端工厂
 * <p>支持多 endpoint / 多 accessKey 的动态客户端池</p>
 */
@Slf4j
@Component
public class S3ClientFactory {

    private final ConcurrentHashMap<String, S3Client> CLIENT_CACHE = new ConcurrentHashMap<>();

    public S3Client getClient(StorageDO storage) {
        String key = storage.getEndpoint() + "|" + storage.getAccessKey();
        return CLIENT_CACHE.computeIfAbsent(key, k -> {
            StaticCredentialsProvider auth = StaticCredentialsProvider.create(AwsBasicCredentials.create(storage
                .getAccessKey(), storage.getSecretKey()));
            return S3Client.builder()
                .credentialsProvider(auth)
                .endpointOverride(URI.create(storage.getEndpoint()))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder().chunkedEncodingEnabled(false).build())
                .build();
        });
    }

    @PreDestroy
    public void closeAll() {
        CLIENT_CACHE.values().forEach(SdkAutoCloseable::close);
    }
}
