package com.martinatanasov.converter.service;

import org.springframework.core.io.Resource;

import java.io.IOException;

public sealed interface ImageService permits ImageServiceImpl {

    boolean convertImageToWebp(final byte[] imageInMemory);

    Iterable<String> getImageNames() throws IOException;

    Resource getSingleImage(String name) throws IOException;

}
