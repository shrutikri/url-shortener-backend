package com.portfolio.urlshortener.dto;

import lombok.Data;

@Data
public class UrlShortenRequest {
    private String originalUrl;
}
