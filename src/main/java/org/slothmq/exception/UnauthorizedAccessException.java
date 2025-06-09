package org.slothmq.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String token, String authGroups) {
        super(String.format("Unauthorized access attempt on token %s for access groups %s", token, authGroups));
    }
}
