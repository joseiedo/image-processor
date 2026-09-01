package br.com.imageprocessor.imageprocessor.controller;

import br.com.imageprocessor.imageprocessor.application.ImageKeys;
import br.com.imageprocessor.imageprocessor.application.ImageProcessCommand;
import br.com.imageprocessor.imageprocessor.application.ImageProcessPublisher;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageOperations;
import br.com.imageprocessor.imagestorage.ImageStorage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageProcessingControllerTest {

    private final ImageProcessPublisher publisher = Mockito.mock(ImageProcessPublisher.class);
    private final ImageStorage imageStorage = Mockito.mock(ImageStorage.class);
    private final ImageProcessingController controller = new ImageProcessingController(publisher, imageStorage);

    @Test
    void publishesCommandAndReturnsAccepted() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2, 3});

        ResponseEntity<Void> response = controller.process(ImageOperations.RESIZE, "{\"width\":100}", file);

        assertEquals(202, response.getStatusCode().value());
        verify(publisher).publish(any(ImageProcessCommand.class));
    }

    @Test
    void returnsProcessedImage() {
        UUID id = UUID.randomUUID();
        byte[] bytes = {1, 2, 3, 4};
        when(imageStorage.load(ImageKeys.processed(id))).thenReturn(Optional.of(bytes));

        ResponseEntity<byte[]> response = controller.getProcessedImage(id);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertArrayEquals(bytes, response.getBody());
    }

    @Test
    void returnsNotFoundWhenImageIsNotReady() {
        UUID id = UUID.randomUUID();
        when(imageStorage.load(ImageKeys.processed(id))).thenReturn(Optional.empty());

        ResponseEntity<byte[]> response = controller.getProcessedImage(id);

        assertEquals(404, response.getStatusCode().value());
    }
}
