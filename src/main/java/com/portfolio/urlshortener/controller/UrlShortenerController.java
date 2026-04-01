package com.portfolio.urlshortener.controller;

import com.portfolio.urlshortener.dto.UrlShortenRequest;
import com.portfolio.urlshortener.dto.UrlShortenResponse;
import com.portfolio.urlshortener.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*") // Handled by CorsConfig, but explicit here too
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    /**
     * Standard REST endpoint for shortening a URL
     */
    @PostMapping("/api/v1/urls/shorten")
    public ResponseEntity<UrlShortenResponse> shortenUrl(@RequestBody UrlShortenRequest request) {
        if (request.getOriginalUrl() == null || request.getOriginalUrl().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String shortCode = urlShortenerService.shortenUrl(request.getOriginalUrl());
        // For development, we return just the path or a full localhost URL
        // In production, this would be the assigned domain like https://shr.ti/
        String shortUrl = "http://localhost:8080/" + shortCode;
        
        return ResponseEntity.ok(new UrlShortenResponse(shortUrl));
    }

    /**
     * Root endpoint to handle the redirection
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectUrl(@PathVariable String shortCode) {
        Optional<String> originalUrlOpt = urlShortenerService.getOriginalUrl(shortCode);

        if (originalUrlOpt.isPresent()) {
            String originalUrl = originalUrlOpt.get();
            // Prefix http if missing
            if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
                originalUrl = "http://" + originalUrl;
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(originalUrl))
                    .build();
        }

        return ResponseEntity.notFound().build();
    }
}
