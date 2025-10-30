package dev.hamez.dataprocessing.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        // Generic fallback for unexpected exceptions. Log full stack for debugging.
        logger.error("Unhandled exception", ex);
        ErrorResponse er = new ErrorResponse(LocalDateTime.now().toString(), "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(er);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        // Do not log stack trace for 404s created by missing endpoints; return a clean 404 JSON.
        logger.debug("No handler found for request: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        ErrorResponse er = new ErrorResponse(LocalDateTime.now().toString(), "Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(er);
    }

    public static class ErrorResponse {
        public String timestamp;
        public String message;

        public ErrorResponse(String timestamp, String message) {
            this.timestamp = timestamp;
            this.message = message;
        }
    }
}
