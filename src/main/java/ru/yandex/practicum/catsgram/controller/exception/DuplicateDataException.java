package ru.yandex.practicum.catsgram.controller.exception;

public class DuplicateDataException extends RuntimeException {
    public DuplicateDataException(String message) {
        super(message);
    }
}
