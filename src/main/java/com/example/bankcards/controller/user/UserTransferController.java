package com.example.bankcards.controller.user;


import com.example.bankcards.dto.userFuncs.transfer.TransferRequest;
import com.example.bankcards.dto.userFuncs.transfer.TransferResponse;
import com.example.bankcards.dto.userFuncs.transfer.TransferSummary;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.TransferService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfer API", description = "Управление переводами средств между картами")
@SecurityRequirement(name = "bearerAuth")
public class UserTransferController {

    @Autowired
    TransferService transferService;

    @Operation(
            summary = "Создать перевод средств",
            description = "Выполняет перевод средств между картами пользователя. " +
                    "Отправителем является текущий аутентифицированный пользователь. " +
                    "Требуется достаточный баланс на карте отправителя."
    )
    @ApiResponse(responseCode = "201",
            description = "Перевод успешно создан и обработан",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = TransferResponse.class))
    )
    @PostMapping
    public ResponseEntity<TransferResponse> commitTransfer(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для выполнения перевода",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransferRequest.class))
            )
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(CREATED).body(transferService.createTransfer(request, user.getId()));
    }

    @Operation(
            summary = "Получить историю переводов",
            description = "Возвращает историю переводов текущего пользователя с пагинацией. " +
                    "Сортировка по умолчанию: по дате создания (новые переводы первыми).",
            parameters = {
                    @Parameter(name = "page",
                            description = "Номер страницы (начиная с 0)",
                            example = "0",
                            in = QUERY),
                    @Parameter(name = "size",
                            description = "Количество элементов на странице",
                            example = "10",
                            in = QUERY),
                    @Parameter(name = "sort",
                            description = "Параметры сортировки в формате: property,asc|desc. " +
                                    "Можно указать несколько параметров через запятую. " +
                                    "Пример: 'amount,desc&sort=createdAt,asc' или 'status,asc,createdAt,desc'",
                            example = "createdAt,desc",
                            in = QUERY,
                            allowReserved = true)
            }
    )
    @ApiResponse(responseCode = "200",
            description = "История переводов успешно получена",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TransferSummary.class)))
    @GetMapping
    public ResponseEntity<Page<TransferSummary>> getTransferHistory(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            @Parameter(hidden = true)
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(transferService.getTransferHistory(pageable, user.getId()));
    }
}
