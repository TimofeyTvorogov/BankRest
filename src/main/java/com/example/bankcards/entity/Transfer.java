package com.example.bankcards.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.BinaryOperator;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Data
@Table(name = "transfers", schema = "public")
@Schema(description = "Сущность перевода средств между картами")
public class Transfer {

    @Schema(
            description = "Уникальный идентификатор перевода",
            example = "456",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(
            description = "Карта-отправитель перевода",
            implementation = Card.class
    )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    @Schema(
            description = "Карта-получатель перевода",
            implementation = Card.class
    )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    @Schema(
            description = "Сумма перевода",
            example = "1500.75",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01"
    )
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Schema(
            description = "Статус перевода",
            example = "PENDING",
            allowableValues = {"PENDING", "PROCESSING", "COMPLETED", "FAILED", "CANCELLED", "REVERSED"},
            requiredMode = Schema.RequiredMode.REQUIRED,
            defaultValue = "PENDING"
    )
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.PENDING;

    @Schema(
            description = "Описание или комментарий к переводу",
            example = "Оплата за услуги"
    )
    private String description;

    @Schema(
            description = "Дата и время создания перевода",
            example = "2024-01-15T14:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(
            description = "Дата и время последнего обновления перевода",
            example = "2024-01-15T14:35:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Schema(
            description = "Дата и время начала обработки перевода",
            example = "2024-01-15T14:31:00"
    )
    private LocalDateTime processedAt;

    @Schema(
            description = "Дата и время успешного завершения перевода",
            example = "2024-01-15T14:32:00"
    )
    private LocalDateTime completedAt;

    @Schema(
            description = "Дата и время отмены перевода",
            example = "2024-01-15T14:33:00"
    )
    private LocalDateTime cancelledAt;

    @Schema(
            description = "Пользователь, инициировавший перевод",
            implementation = User.class
    )
    @ManyToOne
    @JoinColumn(name = "initiated_by_user_id", nullable = false)
    private User initiatedBy;

    @Schema(description = "Возможные статусы перевода")
    public enum TransferStatus {
        //Есть статусы, которые не используются, но будут полезны при дальнейшем развитии системы
        @Schema(description = "Перевод создан, ожидает обработки")
        PENDING,

        @Schema(description = "Перевод находится в процессе выполнения")
        PROCESSING,

        @Schema(description = "Перевод успешно завершен")
        COMPLETED,

        @Schema(description = "Перевод не удался из-за ошибки")
        FAILED,

        @Schema(description = "Перевод был отменен системой")
        CANCELLED,

        @Schema(description = "Перевод был отменен и средства возвращены")
        REVERSED
    }

}