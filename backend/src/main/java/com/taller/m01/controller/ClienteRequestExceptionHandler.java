package com.taller.m01.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice(assignableTypes = ClienteController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
/**
 * Converts malformed client-registration transport data into safe 400 responses.
 *
 * <p>It scopes multipart and DTO transport handling to {@link ClienteController}; business rules,
 * duplicate handling and authorization remain in the facade, service and Spring Security.</p>
 */
public class ClienteRequestExceptionHandler {
    record ErrorResponse(String code, String message, Instant timestamp, String path, Map<String, String> validationErrors) { }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /**
     * Maps Bean Validation field failures to a client-safe response.
     *
     * @param ex field validation failure raised before the controller invokes the facade
     * @param request request used only for its API path
     * @return HTTP 400 with field-level messages
     */
    ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Revisa los datos proporcionados.", request, errors);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestPartException.class})
    /**
     * Rejects malformed multipart data without exposing parser internals.
     *
     * @param ex unreadable JSON or missing multipart part
     * @param request request used only for its API path
     * @return controlled HTTP 400 response
     */
    ResponseEntity<ErrorResponse> solicitudInvalida(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "REQUEST_INVALID", "La solicitud de registro no tiene un formato válido.", request, Map.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    /**
     * Enforces the configured 15 MB photo boundary at the multipart layer.
     *
     * @param ex Spring multipart limit failure
     * @param request request used only for its API path
     * @return HTTP 400 with the standard safe photo message
     */
    ResponseEntity<ErrorResponse> fotografiaGrande(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "FOTOGRAFIA_INVALIDA", "La fotografía debe ser JPEG, PNG o WebP y no puede superar los 15 MB.", request, Map.of());
    }

    /** Builds the uniform non-sensitive error body returned by this scoped advice. */
    private ResponseEntity<ErrorResponse> response(HttpStatus status, String code, String message, HttpServletRequest request, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, Instant.now(), request.getRequestURI(), errors));
    }
}
