package br.com.imageprocessor.imageprocessor.domain.operations;

import org.springframework.stereotype.Component;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

@Component
public class GrayscaleOperation implements ImageOperation<NoParams> {

    @Override
    public ImageOperations getOperationType() {
        return ImageOperations.GRAYSCALE;
    }

    @Override
    public Class<NoParams> getParamsType() {
        return NoParams.class;
    }

    @Override
    public BufferedImage apply(BufferedImage image, NoParams params) {
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return op.filter(image, null);
    }
}
