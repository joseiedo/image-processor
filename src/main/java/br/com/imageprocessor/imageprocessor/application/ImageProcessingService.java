package br.com.imageprocessor.imageprocessor.application;

import java.awt.image.BufferedImage;

public interface ImageProcessingService {

    BufferedImage process(ImageProcessCommand command);
}
