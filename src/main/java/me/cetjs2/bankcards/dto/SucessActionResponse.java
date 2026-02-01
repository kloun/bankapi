package me.cetjs2.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Рростой ответ при успешном выполнении действия")
public record SucessActionResponse(
    @Schema(
            description = "Текстовое сообщение об успешном результате операции",
            example = "Карта 550e8400-e29b-41d4-a716-446655440000 успешно заблокирована")
        String message) {}
