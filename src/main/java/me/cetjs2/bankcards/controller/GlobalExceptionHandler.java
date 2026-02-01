package me.cetjs2.bankcards.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import me.cetjs2.bankcards.dto.ErrorResponse;
import me.cetjs2.bankcards.exception.CardNotFoundException;
import me.cetjs2.bankcards.exception.CardPermissionDeniedException;
import me.cetjs2.bankcards.exception.InsufficientFundsException;
import me.cetjs2.bankcards.exception.UsernameNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler({UsernameNotFoundException.class, CardNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
    var error =
        new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  // Обработка ошибок валидации (например, @Valid)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errors);
  }

  // Обработка ошибок доступа
  @ExceptionHandler(CardPermissionDeniedException.class)
  public ResponseEntity<ErrorResponse> handlePermission(CardPermissionDeniedException ex) {
    var error =
        new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  // Обработка ошибки "недостаточно средств"
  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<ErrorResponse> handleFunds(InsufficientFundsException ex) {
    var error =
        new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }
}
