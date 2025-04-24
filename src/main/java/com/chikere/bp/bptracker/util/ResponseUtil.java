package com.chikere.bp.bptracker.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

public final class ResponseUtil {
    private ResponseUtil() {}

    public static <T> ResponseEntity<T> created(T body, UUID id) {
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(id)
            .toUri();
        return ResponseEntity.created(location).body(body);
    }
}