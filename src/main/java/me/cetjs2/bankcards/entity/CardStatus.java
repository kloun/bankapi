package me.cetjs2.bankcards.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус банковской карты")
public enum CardStatus {
  @Schema(description = "Карта активна и доступна для операций")
  ACTIVE,

  @Schema(description = "Карта заблокирована пользователем или банком")
  BLOCKED,

  @Schema(description = "Срок действия карты истек")
  EXPIRED
}
