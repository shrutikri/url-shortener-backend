package com.portfolio.urlshortener.service;

import com.portfolio.urlshortener.model.UrlMapping;
import com.portfolio.urlshortener.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UrlShortenerService {

    private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALLOWED_CHARACTERS.length();

    @Autowired
    private UrlMappingRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * Creates a short URL by saving original URL to DB, grabbing the auto-increment DB ID, 
     * Base62 encoding that ID, and updating the DB and Cache.
     */
    public String shortenUrl(String originalUrl) {
        // Save initial to get ID
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setClicks(0);
        
        // Temporarily set short code to empty as it's required unique and non-null
        // Actually since it's unique, we might have issues if multiple hit. 
        // We can temporarily set it to a UUID, but a better approach is to rely on sequence.
        // For simplicity, we just set a temp random string
        mapping.setShortCode(java.util.UUID.randomUUID().toString());
        mapping = repository.save(mapping);

        // Encode the ID to base 62
        String shortCode = encode(mapping.getId());
        mapping.setShortCode(shortCode);
        repository.save(mapping); // Update with true short code

        // Cache the mapping: shortCode -> originalUrl
        try {
            redisTemplate.opsForValue().set(shortCode, originalUrl, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            // Redis error (e.g. not running), swallow to keep application alive
        }

        return shortCode;
    }

    /**
     * Resolves high-performance short Code lookups.
     * Tries Redis cache first (O(1)). On miss, falls back to Database.
     */
    public Optional<String> getOriginalUrl(String shortCode) {
        // 1. Check Redis Cache
        try {
            String cachedUrl = redisTemplate.opsForValue().get(shortCode);
            if (cachedUrl != null) {
                return Optional.of(cachedUrl);
            }
        } catch (Exception e) {
            // Redis failure, fallback to DB
        }

        // 2. Check Database on Cache Miss
        Optional<UrlMapping> mappingOpt = repository.findByShortCode(shortCode);
        if (mappingOpt.isPresent()) {
            UrlMapping mapping = mappingOpt.get();
            
            // Async track click analytics (synchronous for simplicity but typically async)
            mapping.setClicks(mapping.getClicks() + 1);
            repository.save(mapping);

            // Re-warm cache
            try {
                redisTemplate.opsForValue().set(shortCode, mapping.getOriginalUrl(), 24, TimeUnit.HOURS);
            } catch (Exception e) {}

            return Optional.of(mapping.getOriginalUrl());
        }

        return Optional.empty();
    }

    private String encode(long input) {
        StringBuilder encodedString = new StringBuilder();
        if (input == 0) {
            return String.valueOf(ALLOWED_CHARACTERS.charAt(0));
        }
        while (input > 0) {
            encodedString.append(ALLOWED_CHARACTERS.charAt((int) (input % BASE)));
            input = input / BASE;
        }
        return encodedString.reverse().toString();
    }
}
