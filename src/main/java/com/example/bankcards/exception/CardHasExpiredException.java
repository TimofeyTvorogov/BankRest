package com.example.bankcards.exception;

public class CardHasExpiredException extends RuntimeException {
    public CardHasExpiredException(String cardNum) {
        super("Карта " + cardNum + "просрочена");
    }
    public CardHasExpiredException() {
        super("карта просрочена");
    }
}
