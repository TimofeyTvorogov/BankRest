package com.example.bankcards.controller.user;


import com.example.bankcards.dto.userFuncs.CardFilter;
import com.example.bankcards.dto.userFuncs.CardSearchRequest;
import com.example.bankcards.dto.userFuncs.CardDataUserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.*;
import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("api/cards")
@Tag(name = "User Card API", description = "Управление картами для пользователей")
@SecurityRequirement(name = "bearerAuth")
public class UserCardController {

    @Autowired
    UserCardService cardService;

    @Operation(
            summary = "Получить карты пользователя",
            description = "Возвращает список карт текущего пользователя с фильтрацией, пагинацией и сортировкой. " +
                    "Сортировка по умолчанию: баланс (по убыванию), дата истечения (по убыванию).",
            parameters = {
                    @Parameter(name = "page",
                            description = "Номер страницы (начиная с 0)",
                            example = "0",
                            in = QUERY),
                    @Parameter(name = "size",
                            description = "Количество элементов на странице",
                            example = "5",
                            in = QUERY),
                    @Parameter(name = "sort",
                            description = "Параметры сортировки в формате: property,asc|desc. " +
                                    "Можно указать несколько параметров через запятую. " +
                                    "Пример: 'balance,desc&sort=activeTill,asc'",
                            example = "balance,desc",
                            in = QUERY,
                            allowReserved = true),
                    @Parameter(name = "filter.status",
                            description = "Фильтр по статусу карты",
                            example = "ACTIVE",
                            in = QUERY),
                    @Parameter(name = "filter.minBalance",
                            description = "Минимальный баланс",
                            example = "100.00",
                            in = QUERY),
                    @Parameter(name = "filter.maxBalance",
                            description = "Максимальный баланс",
                            example = "1000.00",
                            in = QUERY)
            }
    )
    @ApiResponse(responseCode = "200",
            description = "Список карт успешно получен",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CardDataUserResponse.class)))
    @GetMapping
    public ResponseEntity<Page<CardDataUserResponse>> getCards(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            @ParameterObject
            CardFilter filter,
            @Parameter(hidden = true)
            @PageableDefault(
                    size = 5,
                    direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<CardDataUserResponse> cards = cardService.getCards(user.getId(), filter, pageable);
        return ResponseEntity.ok(cards);
    }

    @Operation(
            summary = "Поиск карт по номеру",
            description = "Ищет карты по частичному или полному совпадению номера карты. " +
                    "Сортировка по умолчанию: баланс (по убыванию), дата истечения (по убыванию)."
    )
    @ApiResponse(responseCode = "200",
            description = "Результаты поиска успешно получены",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CardDataUserResponse.class))
    )
    @PostMapping("/search")
    public ResponseEntity<Page<CardDataUserResponse>> searchCardsWithCardNum(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Параметры поиска карт",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CardSearchRequest.class))
            )
            @RequestBody CardSearchRequest searchRequest,
            @Parameter(hidden = true)
            @PageableDefault(
                    size = 5,
                    sort = {"balance", "activeTill"},
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<CardDataUserResponse> cards = cardService.searchCardsWithNumber(
                user.getId(),
                searchRequest,
                pageable
        );
        return ResponseEntity.ok(cards);
    }

    @Operation(
            summary = "Запрос на блокировку карты",
            description = "Отправляет запрос на блокировку карты. "
    )
    @ApiResponse(responseCode = "202",
            description = "Запрос на блокировку принят в обработку")
    @PutMapping("/{id}/block")
    public ResponseEntity<Void> requestBlock(
            @Parameter(name = "id",
                    description = "Идентификатор карты",
                    required = true,
                    example = "123",
                    in = PATH)
            @PathVariable("id") Long cardId) {
        cardService.requestBlock(cardId);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Получить баланс карты",
            description = "Возвращает текущий баланс указанной карты пользователя."
    )
    @ApiResponse(responseCode = "200",
            description = "Баланс карты успешно получен",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = BigDecimal.class,
                    example = "1500.75",
                    description = "Текущий баланс карты"))
    )
    @GetMapping("/{id}")
    public ResponseEntity<BigDecimal> getBalance(
            @Parameter(name = "id",
                    description = "Идентификатор карты",
                    required = true,
                    example = "123",
                    in = PATH)
            @PathVariable("id") Long cardId) {
        return ResponseEntity.ok(cardService.getBalance(cardId).balance());
    }
}
