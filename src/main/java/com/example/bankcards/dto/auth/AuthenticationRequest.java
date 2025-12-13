package com.example.bankcards.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на аутентификацию пользователя")
public record AuthenticationRequest(
        @Schema(
                description = "Имя пользователя для входа в систему",
                example = "Иван Иванов",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "имя пользователя обязательно для заоплнения")
        String name,

        @Schema(
                description = "Пароль пользователя",
                example = "mySecurePassword123",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 6
        )
        @Size(min = 6, message = "Пароль должен состоять хотя бы из 6 символов")
        @NotBlank(message = "пароль обязателен для заполнения")
        String password) {

}

