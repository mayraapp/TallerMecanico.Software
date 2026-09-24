package com.taller.m01.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    record ErrorResponse(String code, String message, Instant timestamp, String path, Map<String, String> validationErrors) { }
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> api(ApiException ex, HttpServletRequest request) { return response(ex.getStatus(), ex.getCode(), ex.getMessage(), request, Map.of()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "Revise los datos proporcionados.", request, errors);
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> denied(AccessDeniedException ex, HttpServletRequest request) { return response(HttpStatus.FORBIDDEN, "FORBIDDEN", "No tiene permiso para realizar esta acción.", request, Map.of()); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unknown(Exception ex, HttpServletRequest request) { return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocurrió un error inesperado.", request, Map.of()); }
    private ResponseEntity<ErrorResponse> response(HttpStatus status, String code, String message, HttpServletRequest request, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, Instant.now(), request.getRequestURI(), errors));
    }
}
