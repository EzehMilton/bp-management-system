package com.chikere.bp.bptracker.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleEntityNotFoundExceptionShouldReturnNotFoundStatus() {
        // Given
        String errorMessage = "Entity not found";
        EntityNotFoundException exception = new EntityNotFoundException(errorMessage);

        // When
        ResponseEntity<String> response = globalExceptionHandler.handleEntityNotFoundException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}