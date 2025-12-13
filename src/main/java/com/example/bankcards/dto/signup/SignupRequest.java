package com.example.bankcards.dto.signup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Запрос на регистрацию нового пользователя")
public record SignupRequest(
        @Schema(
                description = "Имя пользователя",
                example = "Иван Иванов",
                requiredMode = REQUIRED,
                minLength = 3,
                maxLength = 50
        )
        @NotBlank(message = "имя пользователя обязательно для заполнения")
        @Size(min = 3, max = 50, message = "имя пользователя должно быть от 3 до 50 символов")
        String name,

        @Schema(
                description = "Пароль пользователя",
                example = "mySecurePassword123",
                requiredMode = REQUIRED,
                minLength = 6
        )
        @NotBlank(message = "пароль обязателен для заполнения")
        @Size(min = 6, message = "пароль должен состоять минимум из 6 символов")
        String password
) {
}