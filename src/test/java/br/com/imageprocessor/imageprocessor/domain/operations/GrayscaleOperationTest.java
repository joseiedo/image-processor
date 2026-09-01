package br.com.imageprocessor.imageprocessor.domain.operations;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrayscaleOperationTest {

    private final GrayscaleOperation operation = new GrayscaleOperation();

    @Test
    void convertsPixelsToGrayscale() {
        BufferedImage source = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        source.setRGB(5, 5, Color.RED.getRGB());

        BufferedImage result = operation.apply(source, new NoParams());

        Color pixel = new Color(result.getRGB(5, 5));
        assertEquals(pixel.getRed(), pixel.getGreen());
        assertEquals(pixel.getGreen(), pixel.getBlue());
    }
}
