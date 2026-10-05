package com.onewordmax.splitsmart.gateway.dto;

import com.onewordmax.splitsmart.gateway.i18n.HasMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.Data;

@Data
public class ErrorResponse<T, M> implements HasMessage {

    private boolean success = false;
    private String message;
    private T error;
    private M meta;

    /* ---------- FACTORY METHODS ---------- */

    public static <T, M> ResponseEntity<ErrorResponse<T, M>> of(
            HttpStatus status,
            String message,
            T errorCode,
            M meta
    ) {
        ErrorResponse<T, M> res = new ErrorResponse<>();
        res.setMessage(message);
        res.setError(errorCode);
        res.setMeta(meta);
        return ResponseEntity.status(status).body(res);
    }

    public static ResponseEntity<?> of(
            HttpStatus status,
            String message,
            String errorCode
    ) {
        return of(status, message, errorCode, null);
    }
}
