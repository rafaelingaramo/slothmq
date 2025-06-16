package org.slothmq.server.web.exception;

import org.slothmq.exception.SlothHttpException;
import org.slothmq.server.web.dto.ErrorDto;

public class SlothExceptionHandler {
    public static ErrorDto parseException(SlothHttpException e) {
        return new ErrorDto(e.httpCode(), e.getMessage());
    }

    public static ErrorDto parseException(Throwable e) {
        return new ErrorDto(500, e.getMessage());
    }
}
