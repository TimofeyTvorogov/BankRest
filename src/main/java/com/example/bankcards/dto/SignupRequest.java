package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest (
        @NotBlank(message = "имя пользователя обязательно для заоплнения")
        @Size(min = 3, max = 50, message = "имя пользователя должно быть от 3 до 50 символов")
        String name,
        @NotBlank(message = "пароль обязателен для заполнения")
        @Size(min = 10, message = "пароль должен состоять минимум из 10 символов")
        String password
) {
}
