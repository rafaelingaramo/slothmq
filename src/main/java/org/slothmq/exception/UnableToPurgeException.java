package org.slothmq.exception;

//* queue exception
public class UnableToPurgeException extends RuntimeException {

    public UnableToPurgeException(String collectionName) {
        super("Unable to purge collection " + collectionName);
    }
}
