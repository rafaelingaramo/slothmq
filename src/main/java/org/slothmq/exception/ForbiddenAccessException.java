package org.slothmq.exception;

//* http exception
public class ForbiddenAccessException extends SlothHttpException {
    public ForbiddenAccessException(String token, String authGroups) {
        super(String.format("Unauthorized access attempt on token %s for access groups %s", token, authGroups));
    }

    @Override
    public int httpCode() {
        return 403;
    }
}
