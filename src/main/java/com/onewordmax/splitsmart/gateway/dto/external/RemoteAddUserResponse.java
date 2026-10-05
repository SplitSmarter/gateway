package com.onewordmax.splitsmart.gateway.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

@Data
public class RemoteAddUserResponse {
    private Long id;
    private String name;

    @JsonProperty("user_type")
    private String userType; // Or use an Enum: UserIdentity userType

    @JsonProperty("avatar_asset_id")
    private UUID avatarAssetId;

    @JsonProperty("avatar_asset_url")
    private String avatarAssetUrl;
}