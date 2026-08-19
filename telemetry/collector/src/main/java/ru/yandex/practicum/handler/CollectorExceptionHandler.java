package ru.yandex.practicum.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.yandex.practicum.exception.ValidationException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class CollectorExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorDto> handleBadRequest(final MethodArgumentNotValidException e) {
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiErrorDto errorResponse = new ApiErrorDto();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.name());
        errorResponse.setReason("Incorrectly made request.");
        errorResponse.setMessage(errors);
        errorResponse.setTimestamp(Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorDto> handleValidation(Exception e) {
        ApiErrorDto errorResponse = new ApiErrorDto();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.name());
        errorResponse.setReason("Incorrectly made request.");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDto> handleConstraintViolationException(ConstraintViolationException e) {
        ApiErrorDto errorResponse = new ApiErrorDto();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.name());
        errorResponse.setReason("Incorrectly made request.");

        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(";"));

        errorResponse.setMessage(message);
        errorResponse.setTimestamp(Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ApiErrorDto> handleJsonProcessingException(JsonProcessingException e) {
        ApiErrorDto errorResponse = new ApiErrorDto();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.name());
        errorResponse.setReason("Incorrectly made request");

        String message = String.format("JsonProcessingException. %s", e.getMessage());

        errorResponse.setMessage(message);
        errorResponse.setTimestamp(Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorDto> handleValidationException(ValidationException e) {
        ApiErrorDto errorResponse = new ApiErrorDto();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.name());
        errorResponse.setReason("Incorrectly made request.");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleInternal(Exception e) {
        ApiErrorDto errorResponse = new ApiErrorDto();
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
        errorResponse.setReason("Internal unknown server error.");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setTimestamp(Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}