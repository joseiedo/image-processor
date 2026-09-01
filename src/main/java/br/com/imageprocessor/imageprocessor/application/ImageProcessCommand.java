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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

public record ImageProcessCommand(UUID id, ImageOperations operation, String paramsJson, byte[] image, String filename) {

    public String cacheKey() {
        String imageHash = sha256(image);
        String paramsHash = paramsJson == null ? "none" : sha256(paramsJson.getBytes(StandardCharsets.UTF_8));
        return "image:" + imageHash + ":" + operation + ":" + paramsHash;
    }

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

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
