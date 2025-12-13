package com.example.bankcards.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String name) {
        super("Пользователь с именем " + name + " не найден");
    }
    public UserNotFoundException(Long id) {
        super("Пользователь с id " + id + " не найден");
    }

    public UserNotFoundException() {
        this("пользователь не найден");
    }
}
