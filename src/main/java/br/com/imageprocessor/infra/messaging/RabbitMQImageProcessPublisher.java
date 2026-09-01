package br.com.imageprocessor.infra.messaging;

import br.com.imageprocessor.imageprocessor.application.ImageProcessCommand;
import br.com.imageprocessor.imageprocessor.application.ImageProcessPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQImageProcessPublisher implements ImageProcessPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQImageProcessPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(ImageProcessCommand command) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.IMAGE_PROCESS_EXCHANGE,
                RabbitMQConfig.IMAGE_PROCESS_ROUTING_KEY,
                command);
    }
}
