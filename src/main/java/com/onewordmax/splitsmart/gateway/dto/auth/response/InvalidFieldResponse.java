package com.onewordmax.splitsmart.gateway.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvalidFieldResponse {
    private String message;
    private String field;
    private String tag;
}
