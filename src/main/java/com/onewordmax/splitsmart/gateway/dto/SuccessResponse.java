package com.onewordmax.splitsmart.gateway.dto;

import com.onewordmax.splitsmart.gateway.i18n.HasMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.Data;

@Data
public class SuccessResponse<T, M> implements HasMessage {

    private boolean success = true;
    private String message;
    private T data;
    private M meta;

    /* ---------- FACTORY METHODS ---------- */

    public static <T, M> ResponseEntity<SuccessResponse<T, M>> of(
            HttpStatus status,
            String message,
            T data,
            M meta
    ) {
        SuccessResponse<T, M> res = new SuccessResponse<>();
        res.setMessage(message);
        res.setData(data);
        res.setMeta(meta);
        return ResponseEntity.status(status).body(res);
    }

}
