package com.prj.beehouse.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception representing a bad request (HTTP 400 - Bad Request).
 * <p>
 * Indicates errors caused by invalid input provided by the client.
 */
@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    /** Error message describing the bad request. */
    private final String message;

    /**
     * Creates a new bad request exception with a specific error message.
     *
     * @param message the message describing the reason for the bad request
     */
    public BadRequestException(String message) {
        super(message);
        this.message = message;
    }
}
