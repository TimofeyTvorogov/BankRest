package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;


import java.time.LocalDate;

import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "cards",schema = "public")
public class Card {

    @Id
    private String cardNum;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    private LocalDate activeTill;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private Double balance;


    public enum CardStatus {
        ACTIVE,
        BLOCKED,
        EXPIRED
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Card card = (Card) o;
        return getCardNum() != null && Objects.equals(getCardNum(), card.getCardNum());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
