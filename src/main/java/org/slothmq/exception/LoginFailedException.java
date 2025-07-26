package org.slothmq.exception;

//http exception
public class LoginFailedException extends SlothHttpException {
    public LoginFailedException(String message) {
        super(message);
    }

    @Override
    public int httpCode() {
        return 401;
    }
}
