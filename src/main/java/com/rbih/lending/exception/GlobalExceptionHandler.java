package com.rbih.lending.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.rbih.lending.dto.response.ErrorResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return badRequest("Validation failed", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException format) {
            String field = format.getPath().stream()
                    .map(ref -> ref.getFieldName() == null ? "[" + ref.getIndex() + "]" : ref.getFieldName())
                    .reduce((a, b) -> a + "." + b)
                    .orElse("body");
            Object[] allowed = format.getTargetType().getEnumConstants();
            String hint = allowed == null ? "invalid value" : "must be one of " + Arrays.toString(allowed);
            return badRequest("Malformed request", List.of(field + ": " + hint));
        }
        return badRequest("Malformed request", List.of("Request body is missing or not valid JSON"));
    }

    @ExceptionHandler(InvalidLoanApplicationException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidLoanApplicationException ex) {
        return badRequest(ex.getMessage(), List.of());
    }

    private ResponseEntity<ErrorResponse> badRequest(String message, List<String> details) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, details));
    }
}
