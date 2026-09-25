package com.prj.beehouse.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception representing a forbidden error (HTTP 403 - Forbidden).
 * <p>
 * Indicates that the authenticated user does not have sufficient permissions
 * to access the requested resource or perform the requested operation.
 */
@Getter
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    /** Error message describing why access is forbidden. */
    private final String message;

    /**
     * Creates a new forbidden exception with a specific error message.
     *
     * @param message the message describing why access is forbidden
     */
    public ForbiddenException(String message) {
        super(message);
        this.message = message;
    }
}
