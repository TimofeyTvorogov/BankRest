package com.example.bankcards.dto.adminFuncs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

import static com.example.bankcards.entity.Card.CardStatus;

@Schema(description = "Ответ с данными карты для администратора")
public record CardDataAdminResponse(
        @Schema(
                description = "Внутренний идентификатор карты",
                example = "123"
        )
        Long id,
        @Schema(
                description = "Маскированный номер карты",
                example = "**** **** **** 3456"
        )
        String maskedCardNum,

        @Schema(
                description = "Дата истечения срока действия карты",
                example = "2025-12-31",
                format = "date"
        )
        LocalDate activeTill,

        @Schema(
                description = "Имя владельца карты",
                example = "Иван Иванов"
        )
        String owner,

        @Schema(
                description = "Статус карты",
                example = "ACTIVE",
                allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"}
        )
        CardStatus status) {
}