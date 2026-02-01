package me.cetjs2.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на аутентификацию пользователя")
public record AuthRequest(
    @Schema(description = "Имя пользователя (логин)", example = "ivan_gold")
        @NotNull(message = "Username не может быть null")
        @NotEmpty(message = "Username не может быть пустым")
        String username,
    @Schema(
            description = "Пароль пользователя",
            example = "P@ssw0rd123",
            format = "password" // Подсказывает Swagger скрыть символы при вводе
            )
        @NotNull(message = "Пароль не может быть null")
        @NotEmpty(message = "Пароль не может быть пустым")
        String password) {}
