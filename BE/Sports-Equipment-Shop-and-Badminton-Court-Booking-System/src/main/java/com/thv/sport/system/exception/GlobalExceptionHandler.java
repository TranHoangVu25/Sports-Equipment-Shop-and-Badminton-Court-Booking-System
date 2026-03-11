package com.thv.sport.system.exception;

import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.exception.ErrorCode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1️⃣ Validation error (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {

        String key = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        ErrorCode errorCode;

        try {
            errorCode = ErrorCode.valueOf(key);
        } catch (Exception e) {
            errorCode = ErrorCode.INVALID_REQUEST;
        }

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    // 2️⃣ IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {

        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .code(ErrorCode.INVALID_REQUEST.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }

    // 3️⃣ RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.builder()
                        .code(ErrorCode.RUNTIME_ERROR.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }

    // 4️⃣ Access denied (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.builder()
                        .code(ErrorCode.UNAUTHORIZED.getCode())
                        .message(ErrorCode.UNAUTHORIZED.getMessage())
                        .build()
        );
    }

    // 5️⃣ Catch all exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.builder()
                        .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                        .message("Internal server error")
                        .build()
        );
    }
}