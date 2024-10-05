package com.martinatanasov.converter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    private final String ENTRY_RESOURCE = "src\\main\\resources\\static\\images\\edgar.jpg";
    public final static String OUTPUT_RESOURCE = "src\\main\\resources\\static\\webp\\";

    @Override
    public boolean convertImageToWebp() {
        try {
            // Load the PNG image from the file system
            BufferedImage pngImage = ImageIO.read(new File(ENTRY_RESOURCE));
            final UUID imageId = uniqueId();
            // Write the image in WebP format to the output path
            File webpFile = new File(OUTPUT_RESOURCE + imageId + ".webp");
            boolean result = ImageIO.write(pngImage, "webp", webpFile);
            if (result) {
                log.info("Conversion successful! WebP image saved at: " + OUTPUT_RESOURCE + imageId);
            } else {
                log.info("Conversion failed.");
            }
            return true;
        } catch (Exception e) {
            log.error("Error during PNG to WebP conversion: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public Iterable<String> getImageNames() throws IOException {
        Path path = Paths.get(OUTPUT_RESOURCE).normalize();
        return Files.list(path)
                .filter(Files::isRegularFile) // Only regular files, not directories
                .map(Path::getFileName)       // Get only the file name, not the full path
                .map(Path::toString)          // Convert Path to String
                .collect(Collectors.toSet());
    }

    @Override
    public Resource getSingleImage(String name) throws IOException {
        Path path = Paths.get(OUTPUT_RESOURCE).resolve(name).normalize();
        if (Files.exists(path) && Files.isRegularFile(path)) {
            log.info("Resource exist!");
            return new UrlResource(path.toUri());
        }
        return null;
    }

    private UUID uniqueId() {
        return UUID.randomUUID();
    }
}
