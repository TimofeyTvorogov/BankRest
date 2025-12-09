package com.example.bankcards.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cards",schema = "public")
public class Card {

    @Id
    private String cardNum;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
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

}
