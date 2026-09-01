package br.com.imageprocessor.imageprocessor.domain.operations;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
public class CropOperation implements ImageOperation<CropParams> {

    @Override
    public ImageOperations getOperationType() {
        return ImageOperations.CROP;
    }

    @Override
    public Class<CropParams> getParamsType() {
        return CropParams.class;
    }

    @Override
    public BufferedImage apply(BufferedImage image, CropParams params) {
        try {
            return Thumbnails.of(image)
                    .sourceRegion(params.x(), params.y(), params.width(), params.height())
                    .size(params.width(), params.height())
                    .asBufferedImage();
        } catch (IOException e) {
            throw new ImageProcessingException(e);
        }
    }
}
