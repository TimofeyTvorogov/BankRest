package com.example.bankcards.dto.admin;

import java.time.LocalDate;

import static com.example.bankcards.entity.Card.CardStatus;

public record CardDataAdminResponse(String maskedCardNum,
                                    LocalDate expiration,
                                    String owner,
                                    CardStatus status) {
}
