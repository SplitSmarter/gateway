package com.onewordmax.splitsmart.gateway.dto.external;

import lombok.Data;
import java.util.Optional;

@Data
public class RemoteSuccessResponse<T, M> {
    private boolean success;
    private String message;
    private T data;
    private M meta;
}