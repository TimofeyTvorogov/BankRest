package com.example.bankcards.dto.userFuncs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.example.bankcards.entity.Card.CardStatus;

@Schema(description = "Фильтр для поиска карт пользователя")
@Builder
public record CardFilter(
        @Schema(
                description = "Статус карты для фильтрации",
                example = "ACTIVE",
                allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"}
        )
        CardStatus status,

        @Schema(
                description = "Начальная дата срока действия для фильтрации (включительно)",
                example = "2024-01-01",
                format = "date"
        )
        LocalDate expiryFrom,

        @Schema(
                description = "Конечная дата срока действия для фильтрации (включительно)",
                example = "2024-12-31",
                format = "date"
        )
        LocalDate expiryTo,

        @Schema(
                description = "Минимальный баланс для фильтрации",
                example = "100.00",
                minimum = "0.00"
        )
        BigDecimal balanceFrom,

        @Schema(
                description = "Максимальный баланс для фильтрации",
                example = "1000.00",
                minimum = "0.00"
        )
        BigDecimal balanceTo
) {}