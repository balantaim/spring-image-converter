package com.martinatanasov.converter.controller;

import com.martinatanasov.converter.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.martinatanasov.converter.service.ImageServiceImpl.OUTPUT_RESOURCE_FOLDER;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ImageController {

    @Autowired
    final ImageService imageService;

    @GetMapping("")
    public ResponseEntity<String> loadImage() {
        boolean isCreated = imageService.convertImageToWebp();
        if (isCreated) {
            log.info("Webp Image created!");
            return new ResponseEntity<>("Image Created!", HttpStatus.OK);
        } else {
            log.error("Failed to create webp image!");
            return new ResponseEntity<>("Failed!", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/webp")
    public ResponseEntity<Iterable<String>> getImages() throws IOException {
        Iterable<String> data = imageService.getImageNames();
        if (data == null) {
            log.error("No resources found!");
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/download/{imageName}")
    public ResponseEntity<Resource> downloadSingleImage(@PathVariable String imageName) throws IOException {
        Resource resource = imageService.getSingleImage(imageName);
        if (resource != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            // Return 404 if the file doesn't exist
            log.error("No resource found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{imageName}")
    public ResponseEntity<Resource> reviewSingleImage(@PathVariable String imageName) throws IOException {
        Resource resource = imageService.getSingleImage(imageName);
        // Determine the content type of the file
        String contentType = Files.probeContentType(Paths.get(OUTPUT_RESOURCE_FOLDER).resolve(imageName).normalize());
        // If the content type could not be determined, default to application/octet-stream
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        if (resource != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } else {
            // Return 404 if the file doesn't exist
            log.error("No resource found!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
