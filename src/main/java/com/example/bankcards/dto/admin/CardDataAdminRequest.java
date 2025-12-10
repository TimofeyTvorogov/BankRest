package com.example.bankcards.dto.admin;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CardDataAdminRequest(
        @NotEmpty(message = "номер карты обязателен к заполнению")
        @Pattern(regexp = "^[0-9]{16}$", message = "номер карты должен состоять из 16 цифр")
        String cardNum,
        @NotNull(message = "Дата обязательна к заполнению")
        @Future(message = "Дата должна быть не раньше текущей")
        LocalDate activeTill,
        @NotEmpty(message = "id владельца обязателен к заполнению")
        Long ownerId,
        @PositiveOrZero(message = "баланс не может быть отрицательным")
        Double balance) {
}
