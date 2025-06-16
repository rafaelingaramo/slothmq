package org.slothmq.exception;

//http exception
public class InvalidUserException extends SlothHttpException {
    public InvalidUserException(String message) {
        super(message);
    }

    @Override
    public int httpCode() {
        return 400;
    }
}
