package br.com.imageprocessor.imageprocessor.domain.operations;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
public class RotateOperation implements ImageOperation<RotateParams> {

    @Override
    public ImageOperations getOperationType() {
        return ImageOperations.ROTATE;
    }

    @Override
    public Class<RotateParams> getParamsType() {
        return RotateParams.class;
    }

    @Override
    public BufferedImage apply(BufferedImage image, RotateParams params) {
        try {
            return Thumbnails.of(image)
                    .scale(1.0)
                    .rotate(params.degrees())
                    .asBufferedImage();
        } catch (IOException e) {
            throw new ImageProcessingException(e);
        }
    }
}
