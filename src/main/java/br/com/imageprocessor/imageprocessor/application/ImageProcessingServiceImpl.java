package br.com.imageprocessor.imageprocessor.application;

import br.com.imageprocessor.imageprocessor.domain.ImageOperationRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private final ImageOperationRegistry registry;
    private final ObjectMapper objectMapper;

    public ImageProcessingServiceImpl(ImageOperationRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @Override
    public BufferedImage process(ImageProcessCommand command) {
        BufferedImage source = command.decode();
        Object params = command.deserializeParams(registry, objectMapper);
        return registry.apply(command.operation(), source, params);
    }
}
