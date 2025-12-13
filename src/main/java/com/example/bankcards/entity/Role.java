package com.example.bankcards.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.proxy.HibernateProxy;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles", schema = "public")
@Schema(description = "Сущность роли в системе")
@Builder
public class Role implements GrantedAuthority {

    @Schema(
            description = "Уникальный идентификатор роли",
            example = "1"
    )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(
            description = "Название роли",
            example = "ROLE_USER",
            allowableValues = {"ROLE_USER", "ROLE_ADMIN"},
            requiredMode = REQUIRED,
            defaultValue = "ROLE_USER"
    )
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;


    @Schema(
            description = "Возвращает строковое название роли для Spring Security",
            example = "ROLE_USER"
    )
    @Override
    public @Nullable String getAuthority() {
        return name.toString();
    }

    public Role(RoleName name) {
        this.name = name;
    }

    @Schema(description = "Возможные имена ролей")
    public enum RoleName {

        @Schema(description = "Роль обычного пользователя")
        ROLE_USER,

        @Schema(description = "Роль администратора")
        ROLE_ADMIN
    }
}
