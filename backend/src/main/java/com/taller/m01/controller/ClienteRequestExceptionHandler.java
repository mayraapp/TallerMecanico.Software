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
public class ClienteRequestExceptionHandler {
    record ErrorResponse(String code, String message, Instant timestamp, String path, Map<String, String> validationErrors) { }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Revisa los datos proporcionados.", request, errors);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestPartException.class})
    ResponseEntity<ErrorResponse> solicitudInvalida(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "REQUEST_INVALID", "La solicitud de registro no tiene un formato válido.", request, Map.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ErrorResponse> fotografiaGrande(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "FOTOGRAFIA_DEMASIADO_GRANDE", "La fotografía no debe superar 5 MB.", request, Map.of());
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String code, String message, HttpServletRequest request, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, Instant.now(), request.getRequestURI(), errors));
    }
}
