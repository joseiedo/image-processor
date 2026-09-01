package br.com.imageprocessor.imageprocessor.domain.operations;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BlurOperationTest {

    private final BlurOperation operation = new BlurOperation();

    @Test
    void keepsDimensionsWhenBlurring() {
        BufferedImage source = new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB);

        BufferedImage result = operation.apply(source, new BlurParams(5));

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(80, result.getHeight());
    }
}
