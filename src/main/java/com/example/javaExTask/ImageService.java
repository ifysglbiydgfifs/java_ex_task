package com.example.javaExTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private FileStorageService storageService;

    public Image uploadImage(MultipartFile file, String newName) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = "";

        int dotIndex = originalFilename != null ? originalFilename.lastIndexOf('.') : -1;
        if (dotIndex != -1) {
            ext = originalFilename.substring(dotIndex);
        }

        String filename = newName + ext;
        String path = storageService.save(file, filename);
        String thumbnailPath = storageService.saveThumbnail(file, filename);

        Image image = new Image();
        image.setThumbnailPath(thumbnailPath);
        image.setOriginalName(file.getOriginalFilename());
        image.setFilename(filename);
        image.setSize(file.getSize());
        image.setUploadDate(LocalDateTime.now());
        image.setContentType(file.getContentType());
        image.setPath(path);

        return imageRepository.save(image);
    }

    public boolean deleteImage(Long id) {
        return imageRepository.findById(id).map(image -> {
            try {
                Files.deleteIfExists(Paths.get(image.getPath()));
                Files.deleteIfExists(Paths.get("uploads/thumb_" + image.getFilename()));
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при удалении файлов", e);
            }
            imageRepository.delete(image);
            return true;
        }).orElse(false);
    }

    public Optional<Image> renameImage(Long id, String newName) {
        return imageRepository.findById(id).map(image -> {
            try {
                String oldFilename = image.getFilename();
                String ext = oldFilename.substring(oldFilename.lastIndexOf('.'));
                String newFilename = newName + ext;

                Path oldPath = Paths.get(image.getPath());
                Path newPath = Paths.get("uploads/" + newFilename);
                Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

                Path oldThumb = Paths.get("uploads/thumb_" + oldFilename);
                Path newThumb = Paths.get("uploads/thumb_" + newFilename);
                Files.move(oldThumb, newThumb, StandardCopyOption.REPLACE_EXISTING);

                image.setFilename(newFilename);
                image.setPath(newPath.toString());
                image.setThumbnailPath(newThumb.toString());

                return imageRepository.save(image);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при переименовании файлов", e);
            }
        });
    }

    public List<Image> getAllImages(String sortBy) {
        return switch (sortBy) {
            case "name" -> imageRepository.findAll(Sort.by("filename"));
            case "size" -> imageRepository.findAll(Sort.by("size"));
            case "date" -> imageRepository.findAll(Sort.by("uploadDate").descending());
            default -> imageRepository.findAll();
        };
    }

    public List<ImageResponseDto> getAllImageDtos(String sortBy) {
        List<Image> images = getAllImages(sortBy);
        return images.stream()
                .map(img -> new ImageResponseDto(
                        img.getId(),
                        img.getFilename(),
                        "/api/images/" + img.getId(),
                        "/uploads/thumb_" + img.getFilename()
                ))
                .toList();
    }

    public Optional<Image> getImage(Long id) {
        return imageRepository.findById(id);
    }
}
