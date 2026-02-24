package org.monkey_business.utility_supervisor.exception;

import lombok.extern.slf4j.Slf4j;
import org.monkey_business.utility_supervisor.service.ErrorNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorNotificationService errorNotificationService;

    public GlobalExceptionHandler(ErrorNotificationService errorNotificationService) {
        this.errorNotificationService = errorNotificationService;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        String endpoint = request.getDescription(false).replace("uri=", "");
        log.error("REST API error at {}: {}", endpoint, ex.getMessage(), ex);

        // Send notification to Telegram
        errorNotificationService.sendRestControllerError(endpoint, "UNKNOWN", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        body.put("path", endpoint);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<Map<String, Object>> handleParseException(
            ParseException ex, WebRequest request) {

        String endpoint = request.getDescription(false).replace("uri=", "");
        log.error("Parse error at {}: {}", endpoint, ex.getMessage(), ex);

        // Send notification to Telegram
        errorNotificationService.sendRestControllerError(endpoint, "UNKNOWN", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Parse Error");
        body.put("message", ex.getMessage());
        body.put("path", endpoint);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(KoltushiClientException.class)
    public ResponseEntity<Map<String, Object>> handleKoltushiClientException(
            KoltushiClientException ex, WebRequest request) {

        String endpoint = request.getDescription(false).replace("uri=", "");
        log.error("Koltushi client error at {}: {}", endpoint, ex.getMessage(), ex);

        // Send notification to Telegram
        errorNotificationService.sendRestControllerError(endpoint, "UNKNOWN", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", "External Service Error");
        body.put("message", "Failed to fetch data from external service");
        body.put("path", endpoint);

        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        String endpoint = request.getDescription(false).replace("uri=", "");
        log.warn("Invalid argument at {}: {}", endpoint, ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", endpoint);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}