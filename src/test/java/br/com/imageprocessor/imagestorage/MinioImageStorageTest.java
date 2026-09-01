package br.com.imageprocessor.imagestorage;

import br.com.imageprocessor.imagestorage.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinioImageStorageTest {

    private final MinioClient minioClient = mock(MinioClient.class);
    private final MinioProperties properties = new MinioProperties(
            "http://localhost:9000", "admin", "password", "image-processor");
    private final MinioImageStorage storage = new MinioImageStorage(minioClient, properties);

    @Test
    void createsBucketWhenMissingAndSavesImage() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        storage.save("processed/1.png", new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));

        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void reusesExistingBucket() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        storage.save("processed/1.png", new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));

        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void usesConfiguredBucketAndKey() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        storage.save("processed/1.png", new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        assertEquals("image-processor", captor.getValue().bucket());
        assertEquals("processed/1.png", captor.getValue().object());
    }

    @Test
    void wrapsPutObjectFailures() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(ImageStorageException.class,
                () -> storage.save("processed/1.png", new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)));
    }

    @Test
    void loadsStoredBytes() throws Exception {
        byte[] expected = {1, 2, 3};
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(new GetObjectResponse(null, "image-processor", null, "processed/1.png",
                        new ByteArrayInputStream(expected)));

        Optional<byte[]> result = storage.load("processed/1.png");

        assertTrue(result.isPresent());
        assertArrayEquals(expected, result.get());
        ArgumentCaptor<GetObjectArgs> captor = ArgumentCaptor.forClass(GetObjectArgs.class);
        verify(minioClient).getObject(captor.capture());
        assertEquals("image-processor", captor.getValue().bucket());
        assertEquals("processed/1.png", captor.getValue().object());
    }

    @Test
    void returnsEmptyWhenKeyDoesNotExist() throws Exception {
        ErrorResponse error = new ErrorResponse("NoSuchKey", null, null, null, null, null, null);
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new ErrorResponseException(error, null, null));

        assertTrue(storage.load("processed/1.png").isEmpty());
    }

    @Test
    void wrapsGetObjectFailures() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("boom"));

        assertThrows(ImageStorageException.class, () -> storage.load("processed/1.png"));
    }
}
