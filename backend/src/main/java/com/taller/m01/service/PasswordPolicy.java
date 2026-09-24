package com.taller.m01.service;

import com.taller.m01.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {
    public void validate(String password, String confirmation) {
        if (!password.equals(confirmation)) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_CONFIRMATION", "La confirmación de contraseña no coincide.");
        if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*") || !password.matches(".*[^A-Za-z0-9].*"))
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_POLICY", "La contraseña debe tener 8 caracteres, mayúscula, minúscula, número y símbolo.");
    }
}
