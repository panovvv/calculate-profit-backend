package com.dachser.profit.adapter.incoming.web;

import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Centralises error handling for the REST adapter, realising the use case's "Data Retrieval Error"
 * alternative flow: the system logs the error and returns a structured message the UI can surface
 * so the user can retry or fix the data.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** Bean Validation failures on the request body. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleValidation(MethodArgumentNotValidException ex) {
    String detail =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return new ApiError(HttpStatus.BAD_REQUEST.value(), detail, Instant.now());
  }

  /** Domain invariant violations (e.g. negative amounts) surfaced as unprocessable input. */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ApiError handleDomainError(IllegalArgumentException ex) {
    return new ApiError(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage(), Instant.now());
  }

  /** Unknown URL: a missing resource is a 404, not a server error. */
  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiError handleNotFound(NoResourceFoundException ex) {
    return new ApiError(HttpStatus.NOT_FOUND.value(), "Resource not found", Instant.now());
  }

  /** Any unexpected failure: log with stack trace, return a generic message. */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiError handleUnexpected(Exception ex) {
    log.error("Unexpected error while processing profit request", ex);
    return new ApiError(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "An unexpected error occurred. Please retry.",
        Instant.now());
  }

  /** Structured error payload returned to the client. */
  public record ApiError(int status, String detail, Instant timestamp) {}
}
