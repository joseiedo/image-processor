package br.com.imageprocessor.imageprocessor.application;

import br.com.imageprocessor.imageprocessor.domain.ImageOperationRegistry;
import br.com.imageprocessor.imagestorage.ImageStorage;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;

@Service
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private final ImageOperationRegistry registry;
    private final ObjectMapper objectMapper;
    private final ImageStorage imageStorage;

    public ImageProcessingServiceImpl(ImageOperationRegistry registry, ObjectMapper objectMapper, ImageStorage imageStorage) {
        this.registry = registry;
        this.objectMapper = objectMapper;
        this.imageStorage = imageStorage;
    }

    @Override
    public BufferedImage process(ImageProcessCommand command) {
        BufferedImage source = command.decode();
        Object params = command.deserializeParams(registry, objectMapper);
        BufferedImage result = registry.apply(command.operation(), source, params);
        imageStorage.save(ImageKeys.processed(command.id()), result);
        return result;
    }
}
