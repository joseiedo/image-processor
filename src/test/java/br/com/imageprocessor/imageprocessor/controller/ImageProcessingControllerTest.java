package br.com.imageprocessor.imageprocessor.controller;

import br.com.imageprocessor.imageprocessor.application.ImageProcessCommand;
import br.com.imageprocessor.imageprocessor.application.ImageProcessPublisher;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageOperations;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class ImageProcessingControllerTest {

    @Test
    void publishesCommandAndReturnsAccepted() {
        ImageProcessPublisher publisher = Mockito.mock(ImageProcessPublisher.class);
        ImageProcessingController controller = new ImageProcessingController(publisher);
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2, 3});

        ResponseEntity<Void> response = controller.process(ImageOperations.RESIZE, "{\"width\":100}", file);

        assertEquals(202, response.getStatusCode().value());
        verify(publisher).publish(any(ImageProcessCommand.class));
    }
}
