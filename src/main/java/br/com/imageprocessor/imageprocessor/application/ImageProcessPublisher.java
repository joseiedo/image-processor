package br.com.imageprocessor.imageprocessor.application;

public interface ImageProcessPublisher {

    void publish(ImageProcessCommand command);
}
