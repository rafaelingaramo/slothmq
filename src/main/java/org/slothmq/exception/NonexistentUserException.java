package org.slothmq.exception;

//http exception
public class NonexistentUserException extends SlothHttpException {
    public NonexistentUserException(String message) {
        super(message);
    }

    @Override
    public int httpCode() {
        return 404;
    }
}
