package bul.nik.ldtesttask.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID userId) {
        super(String.format("User with username %s not found", userId));
    }

    public UserNotFoundException(String username) {
        super(String.format("User with username %s not found", username));
    }
}
