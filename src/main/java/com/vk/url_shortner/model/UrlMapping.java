package com.vk.url_shortner.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@Table(name = "url_mapping")
@Builder(toBuilder = true)
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalUrl;
    private String shortKey;
    private LocalDateTime createdAt;
    private int clickCount;

    protected UrlMapping() {
    }

    public UrlMapping(Long id, String originalUrl, String shortKey,
                      LocalDateTime createdAt, int clickCount) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.shortKey = shortKey;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.clickCount = clickCount;
    }
}
