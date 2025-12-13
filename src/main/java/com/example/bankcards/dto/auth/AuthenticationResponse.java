package com.example.bankcards.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ на успешную аутентификацию")
public record AuthenticationResponse(
        @Schema(
                description = "JWT токен для аутентификации в защищенных эндпоинтах",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        )
        String token) {}