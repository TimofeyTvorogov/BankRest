package com.example.bankcards.dto.userFuncs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

import static com.example.bankcards.entity.Card.*;

@Schema(description = "Ответ с данными карты для пользователя")
public record CardDataUserResponse(
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
                description = "Статус карты",
                example = "ACTIVE",
                allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"}
        )
        CardStatus status,

        @Schema(
                description = "Текущий баланс карты",
                example = "1500.75"
        )
        BigDecimal balance
){}