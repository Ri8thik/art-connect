package com.project.artconnect.modules.users.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 100)
    private String displayName;

    private String photoUrl;

    @Size(max = 20)
    private String phone;

    private Boolean darkMode;

    private String locationCity;
    private String locationState;
    private String locationCountry;
    private Double locationLat;
    private Double locationLng;
}

