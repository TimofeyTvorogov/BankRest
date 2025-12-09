package com.example.bankcards.exception;



public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("пользовтель с таким именем уже существует");
    }
}
