package com.example.bankcards.dto.user;

import com.example.bankcards.entity.Card;

import java.time.LocalDate;

import static com.example.bankcards.entity.Card.*;

public record CardDataUserResponse (
        String maskedCardNum,
        LocalDate activeTill,
        CardStatus status,
        Double balance
){}
