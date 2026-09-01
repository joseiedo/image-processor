package br.com.imageprocessor.imageprocessor.application;

import java.util.UUID;

public final class ImageKeys {

    private static final String PROCESSED_PREFIX = "processed/";

    private ImageKeys() {
    }

    public static String processed(UUID id) {
        return PROCESSED_PREFIX + id + ".png";
    }
}
