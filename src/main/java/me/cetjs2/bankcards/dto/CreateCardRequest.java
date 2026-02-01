package me.cetjs2.bankcards.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.YearMonth;

@Schema(description = "Данные для создания новой банковской карты")
public record CreateCardRequest(
    @Schema(description = "Уникальное имя пользователя-владельца", example = "ivan_gold")
        @NotBlank(message = "Имя владельца  карты не может быть пустым")
        String ownerUsername,
    @Schema(description = "Начальный баланс карты при открытии", example = "1000.00")
        @Digits(integer = 17, fraction = 2)
        @PositiveOrZero(message = "Начальный баланс не может быть отрицательным")
        BigDecimal initialBalance,
    @Schema(
            description = "Срок действия карты в формате ММ/ГГ",
            example = "12/28",
            type = "string",
            pattern = "MM/yy")
        @Future(message = "Срок действия карты должен быть в будущем")
        @NotNull(message = "Срок действия обязателен")
        @JsonFormat(pattern = "MM/yy")
        YearMonth expirationDate) {}
