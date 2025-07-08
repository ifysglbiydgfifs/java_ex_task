package com.example.javaExTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private FileStorageService storageService;

    @GetMapping("/")
    public String home() {
        return "Welcome!";
    }

    @PostMapping("/upload")
    public ResponseEntity<Image> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam("name") String name) throws IOException {
        return ResponseEntity.ok(imageService.uploadImage(file, name));
    }

    @GetMapping
    public ResponseEntity<List<Image>> list(@RequestParam(defaultValue = "date") String sortBy) {
        return ResponseEntity.ok(imageService.getAllImages(sortBy));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Image> rename(@PathVariable Long id, @RequestParam String newName) {
        return imageService.renameImage(id, newName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dto")
    public ResponseEntity<List<ImageResponseDto>> getImagesDto(@RequestParam(defaultValue = "date") String sortBy) {
        return ResponseEntity.ok(imageService.getAllImageDtos(sortBy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (imageService.deleteImage(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return imageService.getImage(id)
                .map(img -> {
                    Resource file = storageService.load(img.getFilename());
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(img.getContentType()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + img.getOriginalName() + "\"")
                            .body(file);
                }).orElse(ResponseEntity.notFound().build());
    }
}

