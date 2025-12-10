package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "имя пользователя обязательно для заоплнения")
        String name,
        @NotBlank(message = "пароль обязателен для заполнения")
        String password) {

}

