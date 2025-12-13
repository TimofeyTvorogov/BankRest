package com.example.bankcards.dto.adminFuncs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание или обновление пользователя администратором")
public record UserRequest(
        //backlog вынести константы в yml
        @Schema(
                description = "Имя пользователя",
                example = "Иван Иванов",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 2
        )
        @NotBlank(message = "имя пользователя обязательно для заполнения")
        String name,

        @Schema(
                description = "Пароль пользователя",
                example = "mySecurePassword123",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 6
        )
        @NotBlank(message = "пароль обязателен для заполнения")
        @Size(min = 6, message = "Пароль должен состоять хотя бы из 6 символов")
        String password,

        @Schema(
                description = "Роли пользователя в системе",
                example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minContains = 1
        )
        @NotNull(message = "необходимо добавить хотя бы одну роль")
        String[] roles) {

}