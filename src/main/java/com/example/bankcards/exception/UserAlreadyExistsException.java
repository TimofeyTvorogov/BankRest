package com.example.bankcards.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String name) {
        super("Пользователь с именем " + name + " уже существует");
    }

    public UserAlreadyExistsException() {
        super("Пользователь с таким именем уже существует");
    }
}
