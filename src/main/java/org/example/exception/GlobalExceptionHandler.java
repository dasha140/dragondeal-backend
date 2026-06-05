package org.example.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.exception.EmailDeliveryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 400,
                        "error", "Bad Request",
                        "message", exception.getMessage(),
                        "path", request.getRequestURI()
                ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 404,
                        "error", "Not Found",
                        "message", exception.getMessage(),
                        "path", request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<Map<String, String>> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> Map.of(
                        "field", fieldError.getField(),
                        "message", fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Validation failed"
                ))
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Validation Error");
        body.put("message", "Request validation failed");
        body.put("path", request.getRequestURI());
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<Map<String, Object>> handleEmailDeliveryException(
            EmailDeliveryException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 503,
                        "error", "Email Service Unavailable",
                        "message", "Не удалось отправить код подтверждения. Попробуйте позже.",
                        "path", request.getRequestURI()
                ));
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<Map<String, Object>> handleMailException(
            MailException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 503,
                        "error", "Email Service Unavailable",
                        "message", "Не удалось отправить код подтверждения. Попробуйте позже.",
                        "path", request.getRequestURI()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 503,
                        "error", "Service Not Configured",
                        "message", exception.getMessage(),
                        "path", request.getRequestURI()
                ));
    }
}