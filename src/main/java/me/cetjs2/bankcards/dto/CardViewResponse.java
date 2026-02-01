package me.cetjs2.bankcards.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;
import me.cetjs2.bankcards.entity.CardStatus;

@Schema(description = "Информация о банковской карте (публичное представление)")
public record CardViewResponse(
    @Schema(
            description = "Уникальный идентификатор карты",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
    @Schema(description = "Маскированный номер карты", example = "4444 4*** **** 1234")
        String cardNumber,
    @Schema(description = "Имя владельца карты", example = "ivan_gold") String ownerName,
    @Schema(description = "Текущий статус карты", example = "ACTIVE") CardStatus cardStatus,
    @Schema(description = "Дата истечения срока действия", example = "12/28", type = "string")
        @JsonFormat(pattern = "MM/yy")
        YearMonth expirationDate,
    @Schema(description = "Текущий баланс", example = "1500.50") BigDecimal balance) {}
