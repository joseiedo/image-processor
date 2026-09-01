package br.com.imageprocessor.imageprocessor.application;

import br.com.imageprocessor.imageprocessor.domain.ImageOperationRegistry;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageOperations;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageProcessingException;
import br.com.imageprocessor.imageprocessor.domain.operations.NoParams;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

public record ImageProcessCommand(UUID id, ImageOperations operation, String paramsJson, byte[] image, String filename) {

    public BufferedImage decode() {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
            if (bufferedImage == null) {
                throw new ImageProcessingException(new IllegalArgumentException("Unable to decode image"));
            }
            return bufferedImage;
        } catch (IOException e) {
            throw new ImageProcessingException(e);
        }
    }

    public Object deserializeParams(ImageOperationRegistry registry, ObjectMapper objectMapper) {
        Class<?> paramsType = registry.getOperation(operation).getParamsType();
        if (paramsType == NoParams.class) {
            return new NoParams();
        }
        try {
            return objectMapper.readValue(paramsJson, paramsType);
        } catch (JacksonException e) {
            throw new ImageProcessingException(e);
        }
    }
}
