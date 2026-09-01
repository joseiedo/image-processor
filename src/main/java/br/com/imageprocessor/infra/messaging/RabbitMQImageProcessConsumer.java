package br.com.imageprocessor.infra.messaging;

import br.com.imageprocessor.imageprocessor.application.ImageProcessCommand;
import br.com.imageprocessor.imageprocessor.application.ImageProcessingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQImageProcessConsumer {

    private final ImageProcessingService service;

    public RabbitMQImageProcessConsumer(ImageProcessingService service) {
        this.service = service;
    }

    @RabbitListener(queues = RabbitMQConfig.IMAGE_PROCESS_QUEUE)
    public void onImageProcess(ImageProcessCommand command) {
        service.process(command);
    }
}
