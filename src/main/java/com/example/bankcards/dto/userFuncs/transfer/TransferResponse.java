package com.example.bankcards.dto.userFuncs.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Ответ с данными выполненного перевода")
public record TransferResponse(
        @Schema(
                description = "Идентификатор перевода",
                example = "789"
        )
        Long id,

        @Schema(
                description = "Маскированный номер карты-отправителя",
                example = "**** **** **** 1111"
        )
        String fromCardMasked,

        @Schema(
                description = "Маскированный номер карты-получателя",
                example = "**** **** **** 2222"
        )
        String toCardMasked,

        @Schema(
                description = "Сумма перевода",
                example = "1500.75"
        )
        BigDecimal amount,

        @Schema(
                description = "Статус перевода",
                example = "COMPLETED",
                allowableValues = {"PENDING", "PROCESSING", "COMPLETED", "FAILED", "CANCELLED", "REVERSED"}
        )
        String status,

        @Schema(
                description = "Комментарий или описание перевода",
                example = "Оплата за услуги"
        )
        String description,

        @Schema(
                description = "Дата и время создания перевода",
                example = "2024-01-15T14:30:00",
                format = "date-time"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Дата и время обработки перевода",
                example = "2024-01-15T14:31:00",
                format = "date-time"
        )
        LocalDateTime processedAt
) {}