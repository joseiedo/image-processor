package br.com.imageprocessor.imageprocessor;

import br.com.imageprocessor.imageprocessor.operations.ImageOperation;
import br.com.imageprocessor.imageprocessor.operations.ImageOperations;
import br.com.imageprocessor.imageprocessor.operations.ResizeParams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageOperationRegistryTest {

    @Test
    @DisplayName("apply should throw when no operation is found")
    void testApplyOperationThrowsWhenNoOperationFound() {
        ImageOperationRegistry registry = new ImageOperationRegistry(List.of());
        assertThrows(IllegalArgumentException.class, () -> registry.apply(ImageOperations.GRAYSCALE, null, null));
    }

    @Test
    @DisplayName("apply should throw when params do not match the operation type")
    void testApplyOperationThrowsWhenParamsTypeDoesNotMatch() {
        ImageOperation operation = Mockito.mock(ImageOperation.class);
        when(operation.getOperationType()).thenReturn(ImageOperations.RESIZE);
        when(operation.getParamsType()).thenReturn(ResizeParams.class);
        ImageOperationRegistry registry = new ImageOperationRegistry(List.of(operation));

        assertThrows(IllegalArgumentException.class, () -> registry.apply(ImageOperations.RESIZE, null, null));
    }

    @Test
    @DisplayName("apply should not throw when operation is found")
    void testApplyOperationDoesNotThrowWhenOperationFound() {
        ImageOperation operation = Mockito.mock(ImageOperation.class);
        when(operation.getOperationType()).thenReturn(ImageOperations.RESIZE);
        when(operation.getParamsType()).thenReturn(ResizeParams.class);
        ImageOperationRegistry registry = new ImageOperationRegistry(List.of(operation));

        assertDoesNotThrow(() -> registry.apply(ImageOperations.RESIZE, null, new ResizeParams(100, 100)));
    }

    @Test
    @DisplayName("ImageOperationRegistry should call apply on the operation when found")
    void testApplyOperationCallsApplyOnOperation() {
        ImageOperation operation = Mockito.mock(ImageOperation.class);
        when(operation.getOperationType()).thenReturn(ImageOperations.RESIZE);
        when(operation.getParamsType()).thenReturn(ResizeParams.class);
        ImageOperationRegistry registry = new ImageOperationRegistry(List.of(operation));

        ResizeParams params = new ResizeParams(100, 100);
        registry.apply(ImageOperations.RESIZE, null, params);

        verify(operation).apply(null, params);
    }
}
