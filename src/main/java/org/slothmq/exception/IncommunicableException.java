package org.slothmq.exception;

//Queue Exception
public class IncommunicableException extends RuntimeException {
    public IncommunicableException(Exception e) {
        super(e);
    }
}
