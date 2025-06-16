package org.slothmq.exception;

public class UnauthorizedAccessException extends SlothHttpException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    @Override
    public int httpCode() {
        return 401;
    }
}
