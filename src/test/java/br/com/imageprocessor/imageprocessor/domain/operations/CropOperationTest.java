package br.com.imageprocessor.imageprocessor.domain.operations;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CropOperationTest {

    private final CropOperation operation = new CropOperation();

    @Test
    void cropsToTheGivenRegion() {
        BufferedImage source = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);

        BufferedImage result = operation.apply(source, new CropParams(10, 20, 50, 25));

        assertEquals(50, result.getWidth());
        assertEquals(25, result.getHeight());
    }
}
