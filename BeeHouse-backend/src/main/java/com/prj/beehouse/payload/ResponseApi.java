package com.prj.beehouse.payload;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard response envelope used by every controller in the application.
 * <p>
 * Wraps the actual payload (or a message, for operations with no data to
 * return) together with the timestamp and outcome of the request, so every
 * endpoint's JSON response has a consistent shape.
 *
 * @param <T> the type of payload carried by the response envelope
 */
@Builder
@Getter
public class ResponseApi<T> {

    /** Instant the response was generated. */
    private LocalDateTime timestamp;

    /**
     * Outcome status of the request. Serialized as the enum's name
     * (e.g. {@code "OK"}, {@code "BAD_REQUEST"}), not its numeric code.
     */
    private HttpStatus httpStatus;

    /** Human-readable name of the status (e.g. "OK", "Bad Request"); optional but convenient for clients. */
    private String error;

    /** Human-readable message describing the outcome, or an error description. */
    private String message;

    /**
     * Field-keyed validation errors (DTO property → message), set only by the
     * validation handlers in {@code ExceptionManagement}. Lets the frontend
     * attach each error to its own input instead of showing one raw string.
     * Omitted from the JSON when null (jackson non_null inclusion).
     */
    private Map<String, String> errors;

    /** The actual response payload, when the operation returns data. */
    private T data;

    public static <T> ResponseEntity<ResponseApi<T>> buildResponse(HttpStatus status, T data) {
        ResponseApi<T> response = ResponseApi.<T>builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .data(data)
                .build();
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
}
