package me.cetjs2.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Запрос на перевод денежных средств между картами")
public record TransferRequest(
    @Schema(description = "ID карты отправителя", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "ID карты отправителя обязателен")
        UUID fromCardId,
    @Schema(description = "ID карты получателя", example = "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
        @NotNull(message = "ID карты получателя обязателен")
        UUID toCardId,
    @Schema(description = "Сумма перевода", example = "500.00")
        @NotNull(message = "Сумма перевода обязательна")
        @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше нуля")
        BigDecimal sum) {}
