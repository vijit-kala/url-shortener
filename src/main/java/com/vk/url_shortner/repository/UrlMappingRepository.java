package com.vk.url_shortner.repository;

import com.vk.url_shortner.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortKey(String shortKey);

    List<UrlMapping> findAll();
}
