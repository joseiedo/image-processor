package br.com.imageprocessor.imageprocessor.application;

import br.com.imageprocessor.imageprocessor.domain.ImageOperationRegistry;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageProcessingException;
import br.com.imageprocessor.imagestorage.ImageStorage;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Service
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private final ImageOperationRegistry registry;
    private final ObjectMapper objectMapper;
    private final ImageStorage imageStorage;
    private final ProcessedImageCache cache;

    public ImageProcessingServiceImpl(ImageOperationRegistry registry, ObjectMapper objectMapper,
                                      ImageStorage imageStorage, ProcessedImageCache cache) {
        this.registry = registry;
        this.objectMapper = objectMapper;
        this.imageStorage = imageStorage;
        this.cache = cache;
    }

    @Override
    public BufferedImage process(ImageProcessCommand command) {
        String cacheKey = command.cacheKey();
        Optional<byte[]> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            imageStorage.save(ImageKeys.processed(command.id()), cached.get());
            return decode(cached.get());
        }

        BufferedImage source = command.decode();
        Object params = command.deserializeParams(registry, objectMapper);
        BufferedImage result = registry.apply(command.operation(), source, params);

        imageStorage.save(ImageKeys.processed(command.id()), result);
        cache.put(cacheKey, encode(result));
        return result;
    }

    private static byte[] encode(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ImageProcessingException(e);
        }
    }

    private static BufferedImage decode(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new ImageProcessingException(new IllegalArgumentException("Unable to decode image"));
            }
            return image;
        } catch (IOException e) {
            throw new ImageProcessingException(e);
        }
    }
}
