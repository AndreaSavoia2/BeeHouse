package com.prj.beehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception representing a duplicate-resource conflict (HTTP 409 - Conflict).
 * <p>
 * Used when a duplicate resource error does not map cleanly to the
 * {@link ConflictException} resource-field-value structure.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    /**
     * Creates a new duplicate resource exception with a specific error message.
     *
     * @param message the message describing the duplicate resource
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
