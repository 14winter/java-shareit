package ru.practicum.shareit.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super(createMessage(userId));
    }

    private static String createMessage(Long userId) {
        return String.format("Пользователя с id %d не существует.", userId);
    }
}
