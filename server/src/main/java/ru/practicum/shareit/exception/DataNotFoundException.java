package ru.practicum.shareit.exception;

public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(Long dataId) {
        super(createMessage(dataId));
    }

    private static String createMessage(Long dataId) {
        return String.format("Вещь с id %d не существует.", dataId);
    }
}
