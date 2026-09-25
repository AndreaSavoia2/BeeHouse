package com.prj.beehouse.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Generic exception representing a conflict error (HTTP 409 - Conflict).
 * <p>
 * Used as a fallback for unexpected or generic conflict situations that do
 * not have a more specific exception type.
 */
@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class GenericException extends  RuntimeException{

    /** Error message describing the generic exception. */
    private String msg;

    /**
     * Creates a new generic exception with a custom message.
     *
     * @param msg the error message describing the exception
     */
    public GenericException(String msg) {
        super(msg);
        this.msg = msg;
    }
}
