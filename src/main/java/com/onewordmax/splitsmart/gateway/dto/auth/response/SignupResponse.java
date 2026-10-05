package com.onewordmax.splitsmart.gateway.dto.auth.response;

import com.onewordmax.splitsmart.gateway.dto.external.RemoteUserProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponse {
    private Long id;
    private String username;
    private RemoteUserProfileResponse profile;
}
