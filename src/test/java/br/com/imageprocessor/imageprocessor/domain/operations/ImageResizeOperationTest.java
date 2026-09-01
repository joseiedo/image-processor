package br.com.imageprocessor.imageprocessor.domain.operations;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageResizeOperationTest {

    private final ImageResizeOperation operation = new ImageResizeOperation();

    @Test
    void appliesResizeWithGivenDimensions() {
        BufferedImage source = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);

        BufferedImage result = operation.apply(source, new ResizeParams(50, 25));

        assertEquals(50, result.getWidth());
        assertEquals(25, result.getHeight());
    }
}
