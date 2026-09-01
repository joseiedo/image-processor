package br.com.imageprocessor.imageprocessor.operations;

import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

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
        return image;
    }
}
