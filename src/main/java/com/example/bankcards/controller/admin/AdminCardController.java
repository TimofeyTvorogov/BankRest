package com.example.bankcards.controller.admin;


import com.example.bankcards.dto.adminFuncs.CardDataAdminRequest;
import com.example.bankcards.dto.adminFuncs.CardDataAdminResponse;
import com.example.bankcards.service.AdminCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("api/admin/cards")
@Tag(name = "Admin card API", description = "Управление картами для администратора")
@SecurityRequirement(name = "bearerAuth")
public class AdminCardController {

    @Autowired
    AdminCardService adminCardService;

    @Operation(
            summary = "Получить все карты",
            description = "Возвращает список карт с пагинацией. Требует прав администратора.",
            parameters = {
                    @Parameter(name = "page",
                            description = "Номер страницы (начиная с 0)",
                            example = "0",
                            in = ParameterIn.QUERY),
                    @Parameter(name = "size",
                            description = "Количество элементов на странице",
                            example = "5",
                            in = ParameterIn.QUERY),
                    @Parameter(name = "sort",
                            description = "Параметры сортировки в формате: property,asc|desc. " +
                                    "Можно указать несколько параметров через запятую. " +
                                    "Пример: 'id,asc&sort=createdAt,desc' или 'id,desc,createdAt,asc'",
                            example = "id,asc",
                            in = ParameterIn.QUERY,
                            allowReserved = true)
            }
    )
    @ApiResponse(responseCode = "200",
            description = "Список карт успешно получен",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CardDataAdminResponse.class))
    )

    @GetMapping
    public ResponseEntity<Page<CardDataAdminResponse>> getCards(
            @Parameter(hidden = true)
            @PageableDefault(size = 100, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(adminCardService.getCards(pageable));
    }


    @Operation(summary = "получить карту", description = "возвращает карту по id, требует прав администратора")
    @ApiResponse(responseCode = "200",
            description = "Карта успешно получена",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CardDataAdminResponse.class))
    )

    @GetMapping("/{id}")
    public ResponseEntity<CardDataAdminResponse> getCard(
            @Parameter(name = "id",description = "id карты",example = "123",in = ParameterIn.PATH)
            @PathVariable("id")
            Long id) {
        return ResponseEntity.ok(adminCardService.getCardById(id));
    }

    @Operation(
        summary = "Создать новую карту",
        description = "Создаёт новую карту в системе. Требует прав администратора."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Карта успешно создана",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CardDataAdminResponse.class))
    )
    @PostMapping
    public ResponseEntity<CardDataAdminResponse> postCard(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Данные для создания карты",
                required = true,
                content = @Content(schema = @Schema(implementation = CardDataAdminRequest.class))
            )
            @RequestBody @Validated CardDataAdminRequest request) {
        return ResponseEntity.status(CREATED)
                .body(adminCardService.addCard(request));
    }

    @Operation(
        summary = "Активировать карту",
        description = "Активирует карту по ID, если срок действия не прошёл. Требует прав администратора."
    )
    @ApiResponse(responseCode = "204", description = "Карта успешно активирована")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCard(
            @Parameter(name = "id",
                description = "ID карты",
                required = true,
                example = "123",
                in = ParameterIn.PATH)
            @PathVariable("id") Long cardId) {
        adminCardService.activateCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Заблокировать карту",
        description = "Блокирует карту по ID. Требует прав администратора."
    )
    @ApiResponse(responseCode = "204", description = "Карта успешно заблокирована")
    @PatchMapping("/{id}/block")
    public ResponseEntity<Void> blockCard(
            @Parameter(name = "id",
                    description = "ID карты",
                    required = true,
                    example = "123",
                    in = ParameterIn.PATH)
            @PathVariable("id") Long cardId) {
        adminCardService.blockCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Удалить карту",
        description = "Удаляет карту по ID. Требует прав администратора."
    )
    @ApiResponse(responseCode = "204", description = "Карта успешно удалена")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(
            @Parameter(name = "id",
                    description = "ID карты",
                    required = true,
                    example = "123",
                    in = ParameterIn.PATH)
            @PathVariable("id") Long cardId) {
        adminCardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}
