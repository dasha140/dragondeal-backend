package org.example.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ApplicationStartupConfig {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartupConfig.class);

    @PostConstruct
    public void ensureUploadsDirectory() {
        Path uploadsPath = Paths.get("uploads");

        if (!Files.exists(uploadsPath)) {
            try {
                Files.createDirectories(uploadsPath);
                log.info("Created uploads/ directory at: {}", uploadsPath.toAbsolutePath());
            } catch (Exception e) {
                log.warn("Failed to create uploads/ directory: {}", e.getMessage());
            }
        }

        Path avatarsPath = Paths.get("uploads", "avatars");
        if (!Files.exists(avatarsPath)) {
            try {
                Files.createDirectories(avatarsPath);
                log.info("Created uploads/avatars/ directory");
            } catch (Exception e) {
                log.warn("Failed to create uploads/avatars/ directory: {}", e.getMessage());
            }
        }
    }
}
