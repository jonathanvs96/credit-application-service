package com.dmx.creditapplication.infrastructure.adapter.in.web.exception;

import com.dmx.creditapplication.domain.exception.BusinessException;
import com.dmx.creditapplication.domain.exception.ErrorCode;
import com.dmx.creditapplication.infrastructure.adapter.in.web.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {


  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.valueOf(
        ex.getErrorCode().getHttpStatus()
    );
    ApiError error = buildApiError(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(status).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> messages = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(err -> err.getField() + ": " + err.getDefaultMessage())
        .toList();

    ApiError error = buildApiError(
        ErrorCode.VALIDATION_ERROR,
        extractValidationMessage(messages),
        request.getRequestURI()
    );

    return ResponseEntity
        .status(HttpStatus.valueOf(ErrorCode.VALIDATION_ERROR.getHttpStatus()))
        .body(error);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                            HttpServletRequest request) {
    List<String> messages = ex.getConstraintViolations()
        .stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .toList();
    ApiError error = buildApiError(
        ErrorCode.VALIDATION_ERROR,
        extractValidationMessage(messages),
        request.getRequestURI()
    );
    return ResponseEntity
        .status(HttpStatus.valueOf(ErrorCode.VALIDATION_ERROR.getHttpStatus()))
        .body(error);
  }

  private ApiError buildApiError(
      ErrorCode code,
      String message,
      String path
  ) {
    HttpStatus status = HttpStatus.valueOf(code.getHttpStatus());
    return new ApiError()
        .timestamp(OffsetDateTime.now())
        .status(status.value())
        .error(status.getReasonPhrase())
        .code(code.name())
        .message(message)
        .path(path);
  }

  private String extractValidationMessage(List<String> messages) {
    return messages.stream()
        .findFirst()
        .orElse("Validation error");
  }

}
