package com.taller.m01.controller;

import jakarta.servlet.http.HttpServletRequest;

final class RequestInfo {
    private RequestInfo() { }
    static String ip(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
