package com.example.bankcards.dto.adminFuncs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Запрос на создание/изменение карты администратором")
public record CardDataAdminRequest(
        @Schema(
                description = "Номер банковской карты",
                example = "1234567890123456",
                requiredMode = Schema.RequiredMode.REQUIRED,
                pattern = "^[0-9]{16}$"
        )
        @NotBlank(message = "номер карты обязателен к заполнению")
        @Pattern(regexp = "^[0-9]{16}$", message = "номер карты должен состоять из 16 цифр")
        String cardNum,

        @Schema(
                description = "Дата истечения срока действия карты",
                example = "2025-12-31",
                format = "date"
        )
        @Future(message = "Дата должна быть не раньше текущей")
        LocalDate activeTill,

        @Schema(
                description = "Идентификатор владельца карты",
                example = "123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "id владельца обязателен к заполнению")
        Long ownerId,

        @Schema(
                description = "Начальный баланс карты",
                example = "1000.00",
                minimum = "0.00"
        )
        @PositiveOrZero(message = "баланс не может быть отрицательным")
        BigDecimal balance) {
}