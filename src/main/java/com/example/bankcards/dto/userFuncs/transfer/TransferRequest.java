package com.example.bankcards.dto.userFuncs.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Schema(description = "Запрос на выполнение перевода между картами")

public record TransferRequest(
        @Schema(
                description = "Идентификатор карты-отправителя",
                example = "123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "ID карты-отправителя обязателен")
        Long fromCardId,

        @Schema(
                description = "Идентификатор карты-получателя",
                example = "456",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "ID карты-получателя обязателен")
        Long toCardId,

        @Schema(
                description = "Сумма перевода",
                example = "1500.75",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0.01"
        )
        @NotNull(message = "Сумма перевода обязательна")
        @Positive(message = "Сумма перевода должна быть больше 0")
        @DecimalMin(value = "0.01", message = "Минимальная сумма перевода - 0.01")
        BigDecimal amount,

        @Schema(
                description = "Комментарий или описание перевода",
                example = "Оплата за услуги",
                maxLength = 255
        )
        @Size(max = 255, message = "Описание не может быть больше 255 символов")
        String description
) {
}