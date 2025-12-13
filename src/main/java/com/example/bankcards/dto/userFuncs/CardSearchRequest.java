package com.example.bankcards.dto.userFuncs;

import io.swagger.v3.oas.annotations.media.Schema;
import com.example.bankcards.entity.Card;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Schema(description = "Запрос для расширенного поиска карт")
public record CardSearchRequest(
        @Schema(
                description = "Номер карты или его часть для поиска",
                example = "1234",
                minLength = 4,
                maxLength = 19
        )
        String searchNumber,

        @Schema(
                description = "Статус карты для фильтрации",
                example = "ACTIVE",
                allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"}
        )
        Card.CardStatus status,

        @Schema(
                description = "Начальная дата срока действия для фильтрации (включительно)",
                example = "2024-01-01",
                format = "date"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate expiryFrom,

        @Schema(
                description = "Конечная дата срока действия для фильтрации (включительно)",
                example = "2024-12-31",
                format = "date"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
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
){}