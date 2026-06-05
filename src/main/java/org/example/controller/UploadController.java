package org.example.controller;

import org.example.dto.response.UploadImageResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    @PostMapping("/listings/images")
    public UploadImageResponse uploadListingImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return saveImage(file, "listings");
    }

    @PostMapping("/avatars")
    public UploadImageResponse uploadAvatar(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return saveImage(file, "avatars");
    }

    private UploadImageResponse saveImage(
            MultipartFile file,
            String folder
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = ".jpg";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID() + extension;

        Path uploadDir = Paths.get("uploads", folder);
        Files.createDirectories(uploadDir);

        Path targetPath = uploadDir.resolve(filename);

        Files.copy(
                file.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return new UploadImageResponse("/uploads/" + folder + "/" + filename);
    }
}