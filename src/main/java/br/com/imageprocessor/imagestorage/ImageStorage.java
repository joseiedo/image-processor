package br.com.imageprocessor.imagestorage;

import java.awt.image.BufferedImage;
import java.util.Optional;

public interface ImageStorage {

    void save(String key, BufferedImage image);

    Optional<byte[]> load(String key);
}
