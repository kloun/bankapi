package me.cetjs2.bankcards.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Роль пользователя", maxLength = 5)
public enum Role {
  @Schema(description = "Пользователь обычный")
  USER,
  @Schema(description = "Админинстратор")
  ADMIN
}
