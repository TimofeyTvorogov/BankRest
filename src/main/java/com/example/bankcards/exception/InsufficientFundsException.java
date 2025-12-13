package com.example.bankcards.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super("недостаточно средств для перевода");
    }
    public InsufficientFundsException(String cardNum, BigDecimal balance, BigDecimal amount) {
        super(String.format("недостаточно средств на карте %s (%s) для перевода в %s рублей", cardNum, balance.toString(), amount.toString()));
    }
}
