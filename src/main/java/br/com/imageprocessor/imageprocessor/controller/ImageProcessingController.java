package br.com.imageprocessor.imageprocessor.controller;

import br.com.imageprocessor.imageprocessor.application.ImageProcessCommand;
import br.com.imageprocessor.imageprocessor.application.ImageProcessPublisher;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageOperations;
import br.com.imageprocessor.imageprocessor.domain.operations.ImageProcessingException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    public ImageProcessingController(ImageProcessPublisher publisher) {
        this.publisher = publisher;
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
        publisher.publish(command);
        return ResponseEntity.accepted()
                .location(URI.create("/api/v1/images/" + command.id()))
                .build();
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new ImageProcessingException(e);
        }
    }
}
