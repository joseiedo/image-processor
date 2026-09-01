package br.com.imageprocessor.imageprocessor.domain.operations;

import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

@Component
public class BlurOperation implements ImageOperation<BlurParams> {

    @Override
    public ImageOperations getOperationType() {
        return ImageOperations.BLUR;
    }

    @Override
    public Class<BlurParams> getParamsType() {
        return BlurParams.class;
    }

    @Override
    public BufferedImage apply(BufferedImage image, BlurParams params) {
        int size = params.radius() * 2 + 1;
        return new ConvolveOp(new Kernel(size, size, gaussianKernel(params.radius())), ConvolveOp.EDGE_NO_OP, null)
                .filter(image, null);
    }

    private static float[] gaussianKernel(int radius) {
        int size = radius * 2 + 1;
        float sigma = Math.max(radius / 2.0f, 0.5f);
        float[] data = new float[size * size];
        float sum = 0;
        int index = 0;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                float value = (float) Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                data[index++] = value;
                sum += value;
            }
        }
        for (int i = 0; i < data.length; i++) {
            data[i] /= sum;
        }
        return data;
    }
}
