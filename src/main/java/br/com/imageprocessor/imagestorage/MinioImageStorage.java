package br.com.imageprocessor.imagestorage;

import br.com.imageprocessor.imagestorage.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Component
public class MinioImageStorage implements ImageStorage {

    private static final Logger log = LoggerFactory.getLogger(MinioImageStorage.class);

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public MinioImageStorage(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @PostConstruct
    void ensureBucketAtStartup() {
        try {
            ensureBucketExists();
        } catch (ImageStorageException e) {
            log.warn("Minio bucket '{}' not available at startup: {}", properties.bucket(), e.getMessage());
        }
    }

    @Override
    public void save(String key, BufferedImage image) {
        save(key, encode(image));
    }

    @Override
    public void save(String key, byte[] content) {
        ensureBucketExists();
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(key)
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .contentType("image/png")
                    .build());
        } catch (Exception e) {
            throw new ImageStorageException(e);
        }
    }

    @Override
    public Optional<byte[]> load(String key) {
        try (InputStream in = minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.bucket())
                .object(key)
                .build());
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return Optional.of(out.toByteArray());
        } catch (ErrorResponseException e) {
            String code = e.errorResponse().code();
            if ("NoSuchKey".equals(code) || "NoSuchBucket".equals(code)) {
                return Optional.empty();
            }
            throw new ImageStorageException(e);
        } catch (Exception e) {
            throw new ImageStorageException(e);
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.bucket()).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket()).build());
            }
        } catch (Exception e) {
            throw new ImageStorageException(e);
        }
    }

    private static byte[] encode(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            throw new ImageStorageException(e);
        }
        return baos.toByteArray();
    }
}
