package com.onewordmax.splitsmart.gateway.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

@Data
public class RemoteUserProfileResponse {
    private Long id;
    private String name;
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String city;
    private String region;
    private String country;
    private String currency;
    private String language;

    // Maps to the 'avatar' object in your Python response
    private RemoteAvatarDetails avatar;

    @JsonProperty("registered_on")
    private String registeredOn;
}

@Data
class RemoteAvatarDetails {
    @JsonProperty("asset_id")
    private UUID assetId;
    @JsonProperty("asset_url")
    private String assetUrl;
}