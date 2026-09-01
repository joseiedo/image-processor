package br.com.imageprocessor.imageprocessor.domain.operations;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
public class ImageResizeOperation implements ImageOperation<ResizeParams> {

    @Override
    public ImageOperations getOperationType() {
        return ImageOperations.RESIZE;
    }

    @Override
    public Class<ResizeParams> getParamsType() {
        return ResizeParams.class;
    }

    @Override
    public BufferedImage apply(BufferedImage image, ResizeParams params) {
        try {
            return Thumbnails.of(image)
                    .size(params.width(), params.height())
                    .asBufferedImage();
        } catch (IOException e) {
            throw new ImageProcessingException(e);
        }
    }
}
