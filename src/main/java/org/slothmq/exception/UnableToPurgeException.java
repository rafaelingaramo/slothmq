package org.slothmq.exception;

public class UnableToPurgeException extends RuntimeException {

    public UnableToPurgeException(String collectionName) {
        super("Unable to purge collection " + collectionName);
    }
}
