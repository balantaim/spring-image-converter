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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.martinatanasov.converter.service.ImageServiceImpl.OUTPUT_RESOURCE_FOLDER;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ImageController {

    @Autowired
    final ImageService imageService;

    @PostMapping(value = "",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> convertImage(@Validated @RequestParam("image") MultipartFile image) {
        try {
            if(!isSupportedFileType(image)){
                log.error("Wrong file format!");
                return new ResponseEntity<>("File format is not supported!", HttpStatus.BAD_REQUEST);
            }
            // Check if the file is not empty
            if (image.isEmpty()) {
                log.error("No image!");
                return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
            }

            // Memory variable to store the image
            final byte[] imageInMemory = image.getBytes();

            boolean isCreated = imageService.convertImageToWebp(imageInMemory);
            if (isCreated) {
                log.info("Webp Image created!");
                return new ResponseEntity<>("Image Created!", HttpStatus.CREATED);
            } else {
                log.error("Failed to create webp image!");
                return new ResponseEntity<>("Failed!", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            return new ResponseEntity<>("Failed to create image", HttpStatus.INTERNAL_SERVER_ERROR);
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

    private boolean isSupportedFileType(final MultipartFile file) {
        // Get the file content type
        final String contentType = file.getContentType();
        final List<String> SUPPORTED_CONTENT_TYPES =
                Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif");
        // Check if the content type is in the supported types
        return SUPPORTED_CONTENT_TYPES.contains(contentType);
    }

}
