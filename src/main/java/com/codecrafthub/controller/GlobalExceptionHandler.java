package com.codecrafthub.controller;

import com.codecrafthub.exception.CourseNotFoundException;
import com.codecrafthub.exception.FileStorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts Java exceptions into clear JSON HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles missing or invalid required fields.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = baseErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed"
        );

        response.put("fields", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handles malformed JSON, invalid date formats, and invalid status values.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(
            HttpMessageNotReadableException exception
    ) {
        String message = "Request body contains invalid JSON or invalid field values";

        /*
         * Jackson wraps errors from CourseStatus.fromValue().
         * This gives the client a more useful message for invalid statuses.
         */
        if (exception.getMessage() != null
                && exception.getMessage().contains("Invalid status")) {
            message = "Invalid status. Allowed values are: "
                    + "\"Not Started\", \"In Progress\", \"Completed\"";
        }

        Map<String, Object> response = baseErrorResponse(
                HttpStatus.BAD_REQUEST,
                message
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handles requests for courses that do not exist.
     */
    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCourseNotFound(
            CourseNotFoundException exception
    ) {
        Map<String, Object> response = baseErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /**
     * Handles errors involving courses.json.
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Map<String, Object>> handleFileStorageError(
            FileStorageException exception
    ) {
        Map<String, Object> response = baseErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Fallback handler for unexpected errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedError(
            Exception exception
    ) {
        Map<String, Object> response = baseErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Creates a consistent error response structure.
     */
    private Map<String, Object> baseErrorResponse(
            HttpStatus status,
            String message
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", Instant.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);

        return response;
    }
}