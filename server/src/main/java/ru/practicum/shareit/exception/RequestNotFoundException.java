package ru.practicum.shareit.exception;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(Long requestId) {
        super(createMessage(requestId));
    }

    private static String createMessage(Long requestId) {
        return String.format("Запроса с id %d не существует.", requestId);
    }
}