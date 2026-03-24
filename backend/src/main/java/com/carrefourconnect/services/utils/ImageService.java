package com.carrefourconnect.services.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    /**
     * Génère une miniature d'une image.
     * 
     * @param input L'image d'origine sous forme de flux
     * @param width Largeur souhaitée
     * @param height Hauteur souhaitée
     * @return Le flux de l'image optimisée
     * @throws IOException Si une erreur survient lors du traitement
     */
    public byte[] genererMiniature(byte[] imageBytes, int width, int height) throws IOException {
        log.debug("Génération d'une miniature: {}x{}", width, height);
        
        try (InputStream is = new ByteArrayInputStream(imageBytes);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            
            Thumbnails.of(is)
                    .size(width, height)
                    .outputFormat("jpg")
                    .toOutputStream(os);
            
            return os.toByteArray();
        }
    }
}
