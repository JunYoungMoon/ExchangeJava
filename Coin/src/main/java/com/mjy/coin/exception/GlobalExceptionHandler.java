package com.mjy.coin.exception;

import com.mjy.coin.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest servletRequest) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        FieldError firstError = fieldErrors.isEmpty() ? null : fieldErrors.get(0);

        ApiResponse response = ApiResponse.builder()
                .status("fail")
                .msg(firstError != null ? firstError.getDefaultMessage() : "Validation failed")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)  // Content-Type 설정
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(HttpServletRequest servletRequest, Exception ex) {
        ApiResponse response = ApiResponse.builder()
                .status("fail")
                .msg(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)  // Content-Type 설정
                .body(response);
    }

    private Map<String, String> getFieldErrorMessages(List<FieldError> fieldErrors) {
        Map<String, String> errorMessages = new HashMap<>();
        for (FieldError fieldError : fieldErrors) {
            errorMessages.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errorMessages;
    }
}

