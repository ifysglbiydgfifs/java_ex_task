package com.example.javaExTask;

public class ImageResponseDto {
    private Long id;
    private String name;
    private String url;
    private String thumbnailUrl;

    public ImageResponseDto(Long id, String name, String url, String thumbnailUrl) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}
