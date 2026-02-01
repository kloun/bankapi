package me.cetjs2.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Объект с описанием ошибки")
public record ErrorResponse(
    @Schema(description = "HTTP статус-код ошибки", example = "404") int status,
    @Schema(
            description = "Детальное сообщение об ошибке для разработчика",
            example = "Карта с ID 550e8400-e29b-41d4-a716-446655440000 не найдена")
        String message,
    @Schema(description = "Точное время возникновения ошибки") LocalDateTime timestamp) {}
