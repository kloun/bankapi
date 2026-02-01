package me.cetjs2.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.cetjs2.bankcards.dto.AuthRequest;
import me.cetjs2.bankcards.dto.ErrorResponse;
import me.cetjs2.bankcards.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Аутентификация", description = "Методы для получения доступа к системе")
public class AuthController {
  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;

  @Operation(
      summary = "Вход в систему",
      description = "Проверяет логин/пароль и возвращает JWT-токен для доступа к защищенным API",
      security = {})
  @ApiResponse(
      responseCode = "200",
      description = "Успешная авторизация",
      content = @Content(schema = @Schema(example = "{\"token\": \"eyJhbGci...\"}")))
  @ApiResponse(
      responseCode = "401",
      description = "Неверный логин или пароль",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/login")
  public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthRequest request) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.username(), request.password()));

      String token = jwtUtil.generateToken(request.username());
      return ResponseEntity.ok(Map.of("token", token));
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "Invalid username or password"));
    }
  }
}
