package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Стандартный ответ об ошибке")
@Data
public class ErrorResponse {

    @Schema(
            description = "Основное сообщение об ошибке"
    )
    private String message;

    @Schema(
            description = "Список детальных ошибок (опционально)"
    )
    private List<String> errors;

    @Schema(
            description = "Время возникновения ошибки",
            example = "2024-01-15T14:30:00",
            format = "date-time"
    )
    private LocalDateTime timestamp;

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, List<String> errors) {
        this.message = message;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }
}