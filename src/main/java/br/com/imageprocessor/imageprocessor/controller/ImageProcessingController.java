package br.com.imageprocessor.imageprocessor.controller;

import br.com.imageprocessor.imageprocessor.application.ImageKeys;
import br.com.imageprocessor.imageprocessor.application.ImageProcessCommand;
import br.com.imageprocessor.imageprocessor.application.ImageProcessPublisher;
import br.com.imageprocessor.imageprocessor.application.ProcessedImageCache;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageOperations;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageProcessingException;
import br.com.imageprocessor.imagestorage.ImageStorage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/images")
public class ImageProcessingController {

    private final ImageProcessPublisher publisher;
    private final ImageStorage imageStorage;
    private final ProcessedImageCache cache;

    public ImageProcessingController(ImageProcessPublisher publisher, ImageStorage imageStorage, ProcessedImageCache cache) {
        this.publisher = publisher;
        this.imageStorage = imageStorage;
        this.cache = cache;
    }

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> process(
            @RequestParam("operation") ImageOperations operation,
            @RequestParam(value = "params", required = false) String paramsJson,
            @RequestPart("file") MultipartFile file) {
        ImageProcessCommand command = new ImageProcessCommand(
                UUID.randomUUID(),
                operation,
                paramsJson,
                readBytes(file),
                file.getOriginalFilename());
        String cacheKey = command.cacheKey();
        if (cache.get(cacheKey).isPresent()) {
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(URI.create("/api/v1/images/by-cache/" + cacheKey))
                    .build();
        }
        publisher.publish(command);
        return ResponseEntity.accepted()
                .location(URI.create("/api/v1/images/" + command.id()))
                .build();
    }

    @GetMapping("/by-cache/{cacheKey}")
    public ResponseEntity<byte[]> getCachedImage(@PathVariable String cacheKey) {
        return cache.get(cacheKey)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getProcessedImage(@PathVariable UUID id) {
        return imageStorage.load(ImageKeys.processed(id))
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new ImageProcessingException(e);
        }
    }
}
