package br.com.imageprocessor.imageprocessor.application;

import br.com.imageprocessor.imageprocessor.domain.ImageOperationRegistry;
import br.com.imageprocessor.imageprocessor.domain.operations.BlurOperation;
import br.com.imageprocessor.imageprocessor.domain.operations.CropOperation;
import br.com.imageprocessor.imageprocessor.domain.operations.GrayscaleOperation;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageOperations;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageResizeOperation;
import br.com.imageprocessor.imageprocessor.domain.operations.RotateOperation;
import br.com.imageprocessor.imagestorage.ImageStorage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class ImageProcessingServiceImplTest {

    private final ImageStorage imageStorage = Mockito.mock(ImageStorage.class);

    private final ImageProcessingServiceImpl service = new ImageProcessingServiceImpl(
            new ImageOperationRegistry(List.of(
                    new ImageResizeOperation(),
                    new RotateOperation(),
                    new CropOperation(),
                    new GrayscaleOperation(),
                    new BlurOperation())),
            new ObjectMapper(),
            imageStorage);

    @Test
    void processesResizeCommandAndStoresResult() throws IOException {
        UUID id = UUID.randomUUID();
        ImageProcessCommand command = new ImageProcessCommand(
                id,
                ImageOperations.RESIZE,
                "{\"width\":50,\"height\":25}",
                encode(new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB)),
                "image.png");

        BufferedImage result = service.process(command);

        assertEquals(50, result.getWidth());
        assertEquals(25, result.getHeight());
        verify(imageStorage).save(eq("processed/" + id + ".png"), any(BufferedImage.class));
    }

    @Test
    void processesGrayscaleCommandWithoutParams() throws IOException {
        ImageProcessCommand command = new ImageProcessCommand(
                UUID.randomUUID(),
                ImageOperations.GRAYSCALE,
                null,
                encode(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)),
                "image.png");

        BufferedImage result = service.process(command);

        assertEquals(10, result.getWidth());
        assertEquals(10, result.getHeight());
        verify(imageStorage).save(any(String.class), any(BufferedImage.class));
    }

    private static byte[] encode(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
