package com.example.bankcards.exception;


import com.example.bankcards.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            Exception e) {
        ErrorResponse response;
        if (e instanceof MethodArgumentNotValidException) {
            List<String> errors = ((MethodArgumentNotValidException) e)
                    .getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList();
            response = new ErrorResponse("Ошибка валидации", errors);
        }
        else {
            response = new ErrorResponse(e.getMessage());
        }

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler({
        UserNotFoundException.class,
        NoSuchElementException.class
    })
    public ResponseEntity<ErrorResponse> handleUserNotFound(Exception e) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));

    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
                .status(FORBIDDEN)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthException(Exception ex) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleDefault(Exception ex) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ex.getMessage()));
    }


}
