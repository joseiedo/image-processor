package br.com.imageprocessor.infra.messaging;

import br.com.imageprocessor.imageprocessor.application.ImageProcessCommand;
import br.com.imageprocessor.imageprocessor.application.ImageProcessingService;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageOperations;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.verify;

class RabbitMQImageProcessConsumerTest {

    @Test
    void delegatesCommandToProcessingService() {
        ImageProcessingService service = Mockito.mock(ImageProcessingService.class);
        RabbitMQImageProcessConsumer listener = new RabbitMQImageProcessConsumer(service);
        ImageProcessCommand command = new ImageProcessCommand(
                UUID.randomUUID(),
                ImageOperations.RESIZE,
                "{\"width\":50,\"height\":25}",
                new byte[]{1},
                "image.png");

        listener.onImageProcess(command);

        verify(service).process(command);
    }
}
