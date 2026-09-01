package br.com.imageprocessor.infra.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String IMAGE_PROCESS_EXCHANGE = "image.process.exchange";
    public static final String IMAGE_PROCESS_QUEUE = "image.process.queue";
    public static final String IMAGE_PROCESS_ROUTING_KEY = "image.process";

    @Bean
    public TopicExchange imageProcessExchange() {
        return new TopicExchange(IMAGE_PROCESS_EXCHANGE);
    }

    @Bean
    public Queue imageProcessQueue() {
        return new Queue(IMAGE_PROCESS_QUEUE);
    }

    @Bean
    public Binding imageProcessBinding(Queue imageProcessQueue, TopicExchange imageProcessExchange) {
        return BindingBuilder.bind(imageProcessQueue).to(imageProcessExchange).with(IMAGE_PROCESS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
