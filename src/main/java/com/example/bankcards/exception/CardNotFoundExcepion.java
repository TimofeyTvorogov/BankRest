package com.example.bankcards.exception;

public class CardNotFoundExcepion extends RuntimeException {
    public CardNotFoundExcepion(String cardNum) {
        super("Карта с номером " + cardNum + " не найдена");
    }
    public CardNotFoundExcepion(Long id) {
        super("Карта с id " + id + " не найдена");
    }
    public CardNotFoundExcepion() {
        super("Карта не найдена");
    }
}
