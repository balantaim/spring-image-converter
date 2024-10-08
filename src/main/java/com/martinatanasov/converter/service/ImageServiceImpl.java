package com.martinatanasov.converter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public final class ImageServiceImpl implements ImageService {

    public final static String OUTPUT_RESOURCE_FOLDER = "src\\main\\resources\\static\\webp\\";

    @Override
    public boolean convertImageToWebp(final byte[] imageInMemory) {
        try {
            // Load the PNG image from the file system
            BufferedImage bufferedImage = convertToBufferedImage(imageInMemory);
            final UUID imageId = uniqueId();
            // Write the image in WebP format to the output path
            File webpFile = new File(OUTPUT_RESOURCE_FOLDER + imageId + ".webp");
            boolean result = ImageIO.write(bufferedImage, "webp", webpFile);
            if (result) {
                log.info("Conversion successful! WebP image saved at: " + OUTPUT_RESOURCE_FOLDER + imageId);
            } else {
                log.info("Conversion failed.");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error during PNG to WebP conversion: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public Iterable<String> getImageNames() throws IOException {
        Path path = Paths.get(OUTPUT_RESOURCE_FOLDER).normalize();
        return Files.list(path)
                .filter(Files::isRegularFile) // Only regular files, not directories
                .map(Path::getFileName)       // Get only the file name, not the full path
                .map(Path::toString)          // Convert Path to String
                .collect(Collectors.toSet());
    }

    @Override
    public Resource getSingleImage(String name) throws IOException {
        Path path = Paths.get(OUTPUT_RESOURCE_FOLDER).resolve(name).normalize();
        if (Files.exists(path) && Files.isRegularFile(path)) {
            log.info("Resource exist!");
            return new UrlResource(path.toUri());
        }
        return null;
    }

    private UUID uniqueId() {
        return UUID.randomUUID();
    }

    private BufferedImage convertToBufferedImage(final byte[] fileData) throws IOException {
        // Convert byte array to BufferedImage using ByteArrayInputStream
        try (ByteArrayInputStream bis = new ByteArrayInputStream(fileData)) {
            return ImageIO.read(bis);
        }
    }

}
