package com.example.bankcards.controller.admin;


import com.example.bankcards.dto.adminFuncs.UserRequest;
import com.example.bankcards.dto.adminFuncs.UserResponse;
import com.example.bankcards.service.UserService;
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

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User API", description = "Управление пользователями для администратора")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    @Autowired
    UserService userService;

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает детальную информацию о пользователе по его идентификатору. Требует прав администратора."
    )
    @ApiResponse(responseCode = "200",
            description = "Пользователь успешно найден",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = UserResponse.class))
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(name = "id",
                    description = "Идентификатор пользователя",
                    required = true,
                    example = "123",
                    in = ParameterIn.PATH)
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @Operation(
            summary = "Получить список пользователей",
            description = "Возвращает список всех пользователей с поддержкой пагинации и сортировки. Требует прав администратора.",
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
                                    "Пример: 'id,asc&sort=email,desc' или 'lastName,asc,firstName,desc'",
                            example = "id,asc",
                            in = ParameterIn.QUERY,
                            allowReserved = true)
            }
    )
    @ApiResponse(responseCode = "200",
            description = "Список пользователей успешно получен",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class)))
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @Parameter(hidden = true)
            @PageableDefault(size = 5, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.findAllUsers(pageable));
    }

    @Operation(
            summary = "Создать нового пользователя",
            description = "Создаёт нового пользователя в системе. Требует прав администратора."
    )
    @ApiResponse(responseCode = "201",
            description = "Пользователь успешно создан",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class)))
    @PostMapping
    public ResponseEntity<UserResponse> postUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRequest.class))
            )
            @RequestBody @Validated UserRequest request) {
        var createdUserDto = userService.createUser(request);
        return ResponseEntity.status(CREATED).body(createdUserDto);
    }

    @Operation(
            summary = "Удалить пользователя",
            description = "Удаляет пользователя по идентификатору. Требует прав администратора."
    )
    @ApiResponse(responseCode = "204",
            description = "Пользователь успешно удалён")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(name = "id",
                    description = "Идентификатор пользователя",
                    required = true,
                    example = "123",
                    in = ParameterIn.PATH)
            @PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Обновить данные пользователя",
            description = "Обновляет информацию о пользователе по его идентификатору. Требует прав администратора."
    )
    @ApiResponse(responseCode = "200",
            description = "Данные пользователя успешно обновлены",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(name = "id",
                    description = "Идентификатор пользователя",
                    required = true,
                    example = "123",
                    in = ParameterIn.PATH)
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRequest.class))
            )
            @RequestBody @Validated UserRequest updateRequest) {
        return ResponseEntity.ok(userService.updateUser(id, updateRequest));
    }

}
