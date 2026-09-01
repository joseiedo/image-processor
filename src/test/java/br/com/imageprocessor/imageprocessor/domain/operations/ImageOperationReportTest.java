package br.com.imageprocessor.imageprocessor.domain.operations;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class ImageOperationReportTest {

    private final Path reportDir = Paths.get("target", "operation-report");

    @Test
    void generatesHtmlReportWithOperationResults() throws IOException {
        Files.createDirectories(reportDir);
        BufferedImage input = loadTestImage();

        Map<String, BufferedImage> results = new LinkedHashMap<>();
        results.put("input", input);
        results.put("resize", new ImageResizeOperation().apply(input, new ResizeParams(200, 150)));
        results.put("rotate", new RotateOperation().apply(input, new RotateParams(90)));
        results.put("crop", new CropOperation().apply(input, new CropParams(50, 40, 200, 150)));
        results.put("grayscale", new GrayscaleOperation().apply(input, new NoParams()));
        results.put("blur", new BlurOperation().apply(input, new BlurParams(10)));

        for (Map.Entry<String, BufferedImage> entry : results.entrySet()) {
            assertNotNull(entry.getValue(), entry.getKey());
            ImageIO.write(entry.getValue(), "png", reportDir.resolve(entry.getKey() + ".png").toFile());
        }

        String html = loadTemplate().replace("{{results}}", buildResultsHtml(results.keySet()));
        Files.writeString(reportDir.resolve("index.html"), html);
    }

    private static BufferedImage loadTestImage() throws IOException {
        try (InputStream in = ImageOperationReportTest.class.getResourceAsStream("/monke.jpg")) {
            if (in == null) {
                fail("Missing test resource /monke.jpg");
            }
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                fail("Unable to decode /monke.jpg");
            }
            return image;
        }
    }

    private static String loadTemplate() throws IOException {
        try (InputStream in = ImageOperationReportTest.class.getResourceAsStream("/operation-report.html")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String buildResultsHtml(Iterable<String> names) {
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            sb.append("<h2>").append(name).append("</h2><img src=\"").append(name).append(".png\">");
        }
        return sb.toString();
    }
}
