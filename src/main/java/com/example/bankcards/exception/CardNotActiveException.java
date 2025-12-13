package com.example.bankcards.exception;

public class CardNotActiveException extends RuntimeException {
    public CardNotActiveException(String cardNum) {
        super("Карта " + cardNum + " заблокирована или просрочена");
    }
    public CardNotActiveException() {
        super("Карта заблокирована или просрочена");
    }
}
