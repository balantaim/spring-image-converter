package com.martinatanasov.converter.service;

import org.springframework.core.io.Resource;

import java.io.IOException;

public interface ImageService {

    boolean convertImageToWebp();

    Iterable<String> getImageNames() throws IOException;

    Resource getSingleImage(String name) throws IOException;

}
