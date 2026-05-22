package com.project.artconnect.modules.users.dto;

import com.project.artconnect.common.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String displayName;
    private String photoUrl;
    private String phone;
    private boolean darkMode;
    private String locationCity;
    private String locationState;
    private String locationCountry;
    private Double locationLat;
    private Double locationLng;
    private Set<UserRole> roles;
    private boolean isArtist;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

