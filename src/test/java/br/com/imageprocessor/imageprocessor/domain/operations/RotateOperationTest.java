package br.com.imageprocessor.imageprocessor.domain.operations;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RotateOperationTest {

    private final RotateOperation operation = new RotateOperation();

    @Test
    void rotatesAndSwapsDimensionsAt90Degrees() {
        BufferedImage source = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);

        BufferedImage result = operation.apply(source, new RotateParams(90));

        assertEquals(100, result.getWidth());
        assertEquals(200, result.getHeight());
    }
}
