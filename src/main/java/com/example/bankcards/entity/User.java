package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", schema = "public")
@Schema(description = "Сущность пользователя системы")
@Builder
public class User implements UserDetails {

    @Schema(
            description = "Уникальный идентификатор пользователя",
            example = "123"
    )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(
            description = "Список банковских карт пользователя",
            implementation = Card.class
    )
    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private List<Card> cards;

    @Schema(
            description = "Список ролей пользователя в системе",
            implementation = Role.class
    )
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")}
    )
    private List<Role> roles;

    @Schema(
            description = "Имя пользователя (используется как username для аутентификации)",
            example = "Иван Иванов",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Column(nullable = false)
    private String name;

    @Schema(
            description = "Пароль пользователя (хранится в хэшированном виде)",
            example = "$2a$10$abcdefghijklmnopqrstuv",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Column(nullable = false)
    private String password;

    @Override
    @Schema(
            description = "Возвращает список ролей как authorities для Spring Security"
    )
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    @Schema(
            description = "Возвращает пароль пользователя"
    )
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    @Schema(
            description = "Возвращает имя пользователя как username для аутентификации",
            example = "Иван Иванов"
    )
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public User(List<Card> cards, List<Role> roles, String name, String password) {
        this.cards = cards;
        this.roles = roles;
        this.name = name;
        this.password = password;
    }

    public void addCard(Card card) {
        cards.add(card);
    }
}
