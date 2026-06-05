package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.UploadImageResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Path LISTING_UPLOAD_DIR = Paths.get(
            System.getProperty("user.dir"),
            "uploads",
            "listings"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public UploadImageResponse uploadListingImage(MultipartFile file) {
        validateImage(file);

        try {
            Files.createDirectories(LISTING_UPLOAD_DIR);

            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;

            Path targetPath = LISTING_UPLOAD_DIR.resolve(filename)
                    .toAbsolutePath()
                    .normalize();

            file.transferTo(targetPath);

            String imageUrl = "/uploads/listings/" + filename;

            return new UploadImageResponse(imageUrl);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to upload image");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image size must be less than 5MB");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPEG, PNG and WEBP images are allowed");
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".png")
                && !extension.equals(".webp")) {
            return ".jpg";
        }

        return extension;
    }
}