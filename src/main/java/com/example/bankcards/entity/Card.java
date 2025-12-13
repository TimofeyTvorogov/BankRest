package com.example.bankcards.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cards", schema = "public")
@Schema(description = "Сущность банковской карты")
@Builder
public class Card {

    @Schema(
            description = "Уникальный идентификатор карты",
            example = "123",
            requiredMode = REQUIRED
    )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(
        description = "Номер банковской карты",
        example = "1234567890123456",
        minLength = 16,
        maxLength = 16,
        requiredMode = REQUIRED
    )
    private String cardNum;

    @Schema(
        description = "Владелец карты",
        implementation = User.class,
        requiredMode = REQUIRED
    )
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User owner;

    @Schema(
        description = "Дата истечения срока действия карты",
        example = "2025-12-31",
        requiredMode = REQUIRED,
        format = "date"
    )
    @Column(updatable = false)
    private LocalDate activeTill = LocalDate.now().plusYears(5);


    @Schema(
        description = "Статус карты. Рассчитывается автоматически на основе даты истечения срока действия",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"},
        defaultValue = "ACTIVE",
        requiredMode = REQUIRED
    )
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.ACTIVE;

    @Schema(
        description = "Текущий баланс карты",
        example = "1500.75",
        minimum = "0.00",
        defaultValue = "0.00"
    )
    @Column(nullable = false, scale = 2)

    private BigDecimal balance = BigDecimal.ZERO;

    @Schema(description = "Возможные статусы карты")
    public enum CardStatus {
        @Schema(description = "Карта активна и может использоваться для операций")
        ACTIVE,

        @Schema(description = "Карта заблокирована администратором или по запросу пользователя")
        BLOCKED,

        @Schema(description = "Срок действия карты истёк")
        EXPIRED
    }

    public Card(String cardNum, User owner, LocalDate activeTill, CardStatus status, BigDecimal balance) {
        this.cardNum = cardNum;
        this.owner = owner;
        this.activeTill = activeTill;
        this.status = status;
        this.balance = balance;
    }

    @Schema(
            description = "Автоматически рассчитываемый статус карты. " +
            "Если карта заблокирована - возвращает BLOCKED. " +
            "Если текущая дата позже даты истечения срока действия - возвращает EXPIRED. " +
            "В остальных случаях - ACTIVE."
    )
    @Transient
    public CardStatus getStatus() {
        if (LocalDate.now().isAfter(activeTill)) {
            return CardStatus.EXPIRED;
        }
        if (status == CardStatus.BLOCKED) {
            return status;
        }
        return CardStatus.ACTIVE;
    }

    @PreUpdate
    @PrePersist
    public void updateExpirationStatus() {
        status = getStatus();
    }
}


