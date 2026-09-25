package com.prj.beehouse.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception representing a conflict error (HTTP 409 - Conflict).
 * <p>
 * Indicates that a resource already exists with a given field value,
 * violating a uniqueness constraint.
 */
@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    /** Name of the resource that caused the conflict. */
    private final String resourceName;

    /** Field used to check uniqueness. */
    private final String fieldName;

    /** Field value that already exists. */
    private final Object fieldValue;

    /**
     * Creates a new conflict exception with details about the conflicting resource.
     *
     * @param resourceName the name of the resource that caused the conflict
     * @param fieldName the field used to check uniqueness
     * @param fieldValue the value of the field that already exists
     */
    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
