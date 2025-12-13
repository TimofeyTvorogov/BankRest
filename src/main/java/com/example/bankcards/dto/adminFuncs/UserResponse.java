package com.example.bankcards.dto.adminFuncs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Ответ с данными пользователя для администратора")
public record UserResponse(
        @Schema(
                description = "Идентификатор пользователя",
                example = "123"
        )
        Long id,

        @Schema(
                description = "Имя пользователя",
                example = "Иван Иванов"
        )
        String name,

        @Schema(
                description = "Хэш пароля пользователя (для отображения в зашифрованном виде)",
                example = "$2a$10$abcdefghijklmnopqrstuv"
        )
        String hash,

        @Schema(
                description = "Список ролей пользователя",
                example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]"
        )
        List<String> roles) {

}