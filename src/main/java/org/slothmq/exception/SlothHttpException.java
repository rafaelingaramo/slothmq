package org.slothmq.exception;

public abstract class SlothHttpException extends RuntimeException {
    public SlothHttpException(String message) {
        super(message);
    }

    abstract public int httpCode();
}
