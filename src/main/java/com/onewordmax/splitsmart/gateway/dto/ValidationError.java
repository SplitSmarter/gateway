package com.onewordmax.splitsmart.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationError {
    private String field;
    private String code;
    private String message;
}
