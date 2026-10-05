package com.onewordmax.splitsmart.gateway.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onewordmax.splitsmart.gateway.constants.UserType;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class RemoteAddUserRequest {
    private String name;
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String city;
    private String country;
    private String currency;
    private String language;

    @JsonProperty("user_type")
    private UserType userType;

    @JsonProperty("avatar_asset_id")
    private UUID avatarAssetId;
}