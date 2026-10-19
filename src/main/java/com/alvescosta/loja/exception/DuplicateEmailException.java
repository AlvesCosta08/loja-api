package com.alvescosta.loja.exception;

public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String message) {
        super(message);
    }
}