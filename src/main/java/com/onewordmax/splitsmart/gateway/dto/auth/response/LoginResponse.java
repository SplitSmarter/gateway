package com.onewordmax.splitsmart.gateway.dto.auth.response;

import com.onewordmax.splitsmart.gateway.dto.external.RemoteUserProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String username;
    private RemoteUserProfileResponse profile;
}
