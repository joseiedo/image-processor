package br.com.imageprocessor.imageprocessor.domain.operations;

import java.awt.image.BufferedImage;

public interface ImageOperation<P> {

    ImageOperations getOperationType();

    Class<P> getParamsType();

    BufferedImage apply(BufferedImage image, P params);
}
