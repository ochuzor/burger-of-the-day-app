package com.ochuzor.burgeroftheday.api;

import com.ochuzor.burgeroftheday.burger.BurgerOfTheDayNotFoundException;
import com.ochuzor.burgeroftheday.user.MissingUsernameException;
import com.ochuzor.burgeroftheday.user.UnknownUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler({MissingUsernameException.class, UnknownUserException.class})
  ResponseEntity<ApiErrorResponse> handleUnauthorized(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiErrorResponse("unauthorized"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
    FieldError fieldError = exception.getBindingResult().getFieldError();

    String message =
        fieldError != null && fieldError.getDefaultMessage() != null
            ? fieldError.getDefaultMessage()
            : "invalid request";

    return ResponseEntity.badRequest().body(new ApiErrorResponse(message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException exception) {
    return ResponseEntity.badRequest().body(new ApiErrorResponse("malformed request"));
  }

  @ExceptionHandler(BurgerOfTheDayNotFoundException.class)
  ResponseEntity<ApiErrorResponse> handleBurgerOfTheDayNotFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiErrorResponse("burger of the day not found"));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException exception) {
    String message =
        switch (exception.getName()) {
          case "publish_date" -> "invalid publish date";
          case "page", "size" -> "invalid pagination";
          default -> "invalid request";
        };

    return ResponseEntity.badRequest().body(new ApiErrorResponse(message));
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  ResponseEntity<ApiErrorResponse> handleMethodValidation(
      HandlerMethodValidationException exception) {
    return ResponseEntity.badRequest().body(new ApiErrorResponse("invalid pagination"));
  }
}
