package br.com.imageprocessor.imageprocessor.domain;

import br.com.imageprocessor.imageprocessor.domain.operations.ImageOperation;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageOperations;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ImageOperationRegistry {

    private final Map<ImageOperations, ImageOperation<?>> operations;

    public ImageOperationRegistry(List<ImageOperation<?>> operations) {
        this.operations = operations.stream()
                .collect(Collectors.toMap(ImageOperation::getOperationType, Function.identity()));
    }

    public ImageOperation<?> getOperation(ImageOperations type) {
        ImageOperation<?> operation = operations.get(type);
        if (operation == null) {
            throw new IllegalArgumentException("Operation not found: " + type);
        }
        return operation;
    }

    public <P> BufferedImage apply(ImageOperations type, BufferedImage image, P params) {
        ImageOperation<?> operation = getOperation(type);
        validateParams(type, operation, params);
        return cast(operation).apply(image, params);
    }

    private <P> void validateParams(ImageOperations type, ImageOperation<?> operation, P params) {
        Class<?> expected = operation.getParamsType();
        if (expected != null && !expected.isInstance(params)) {
            String actual = params == null ? "null" : params.getClass().getSimpleName();
            throw new IllegalArgumentException(
                    "Operation " + type + " expects " + expected.getSimpleName() + " but got " + actual);
        }
    }

    @SuppressWarnings("unchecked")
    private static <P> ImageOperation<P> cast(ImageOperation<?> operation) {
        return (ImageOperation<P>) operation;
    }
}
