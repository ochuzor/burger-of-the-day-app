package com.ochuzor.burgeroftheday.api;

import com.ochuzor.burgeroftheday.user.MissingUsernameException;
import com.ochuzor.burgeroftheday.user.UnknownUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler({MissingUsernameException.class, UnknownUserException.class})
  ResponseEntity<ApiErrorResponse> handleUnauthorized(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiErrorResponse("unauthorized"));
  }
}
