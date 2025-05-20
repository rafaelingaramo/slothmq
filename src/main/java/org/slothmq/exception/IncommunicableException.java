package org.slothmq.exception;

public class IncommunicableException extends RuntimeException {
    public IncommunicableException(Exception e) {
        super(e);
    }
}
