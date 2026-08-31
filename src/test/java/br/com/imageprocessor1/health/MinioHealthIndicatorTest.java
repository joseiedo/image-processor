package br.com.imageprocessor1.health;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinioHealthIndicatorTest {

    @Test
    void healthIsUpWhenMinioReachable() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        when(minioClient.listBuckets()).thenReturn(java.util.List.of());

        MinioHealthIndicator indicator = new MinioHealthIndicator(minioClient);

        assertEquals(Status.UP, indicator.health().getStatus());
        verify(minioClient).listBuckets();
    }

    @Test
    void healthIsDownWhenMinioUnreachable() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        when(minioClient.listBuckets()).thenThrow(new RuntimeException("boom"));

        MinioHealthIndicator indicator = new MinioHealthIndicator(minioClient);

        assertEquals(Status.DOWN, indicator.health().getStatus());
    }
}
