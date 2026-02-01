package me.cetjs2.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Информация о запросе на блокировку карты")
public record BlockRequestViewResponse(
    @Schema(
            description = "ID карты, которую требуется заблокировать",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID cardId,
    @Schema(description = "Имя пользователя, подавшего запрос", example = "ivan_gold")
        String userName,
    @Schema(description = "Дата и время создания запроса") LocalDateTime createdDate,
    @Schema(description = "Дата и время одобрения запроса (null, если еще не одобрен)")
        LocalDateTime approvedDate,
    @Schema(description = "Имя администратора, одобрившего блокировку", example = "admin_super")
        String approvedByUserName) {}
