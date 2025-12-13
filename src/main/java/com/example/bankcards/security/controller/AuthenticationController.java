package com.example.bankcards.security.controller;


import com.example.bankcards.dto.auth.AuthenticationRequest;
import com.example.bankcards.dto.auth.AuthenticationResponse;
import com.example.bankcards.security.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication API", description = "Вход в систему")
public class AuthenticationController {

    @Autowired
    AuthenticationService authenticationService;

    @Operation(
            summary = "Вход в систему",
            description = "Аутентифицирует пользователя по имени и паролю. " +
                    "Возвращает JWT токен для последующей авторизации в защищенных эндпоинтах."
    )
    @ApiResponse(responseCode = "200",
            description = "Успешная аутентификация",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = AuthenticationResponse.class))
    )

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для входа в систему",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthenticationRequest.class))
            )
            @RequestBody @Validated AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.getUser(request));
    }
}