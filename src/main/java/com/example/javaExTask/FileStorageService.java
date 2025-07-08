package com.example.javaExTask;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads");

    public FileStorageService() throws IOException {
        Files.createDirectories(root);
    }

    public String save(MultipartFile file, String filename) throws IOException {
        Path target = root.resolve(filename);
        System.out.println("Saving file to " + target.toAbsolutePath());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File saved successfully");
        return target.toString();
    }


    public String saveThumbnail(MultipartFile file, String thumbnailName) throws IOException {
        Path target = root.resolve("thumb_" + thumbnailName);
        try (InputStream in = file.getInputStream();
             OutputStream out = Files.newOutputStream(target)) {
            Thumbnails.of(in)
                    .size(200, 200)
                    .toOutputStream(out);
        }
        return target.toString();
    }

    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            return new UrlResource(file.toUri());
        } catch (Exception e) {
            throw new RuntimeException("File not found " + filename);
        }
    }
}

