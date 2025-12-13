package com.example.bankcards.security.controller;

import com.example.bankcards.dto.signup.SignupRequest;
import com.example.bankcards.dto.signup.SignupResponse;
import com.example.bankcards.security.service.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/auth")
@Tag(name = "Sign up API", description = "Регистрация в системе")
public class SignupController {

    @Autowired
    SignupService signupService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает новую учетную запись пользователя в системе. " +
                    "После успешной регистрации сразу же возвращает JWT"
    )
    @ApiResponse(responseCode = "201",
            description = "Пользователь успешно зарегистрирован",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SignupResponse.class)))

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signupUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации нового пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SignupRequest.class))
            )
            @RequestBody @Validated SignupRequest request) {
        return ResponseEntity
                .status(CREATED)
                .body(signupService.createUser(request));
    }
}