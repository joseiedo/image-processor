package br.com.imageprocessor.imageprocessor.application;

import java.util.Optional;

public interface ProcessedImageCache {

    Optional<byte[]> get(String key);

    void put(String key, byte[] image);
}
