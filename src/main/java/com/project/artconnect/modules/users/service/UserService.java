package com.project.artconnect.modules.users.service;

import com.project.artconnect.modules.users.dto.UpdateProfileRequest;
import com.project.artconnect.modules.users.dto.UserDto;
import com.project.artconnect.modules.users.entity.User;
import com.project.artconnect.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto getCurrentUser(User user) {
        return mapToDto(user);
    }

    @Transactional
    public UserDto updateProfile(User user, UpdateProfileRequest request) {
        if (request.getDisplayName() != null) user.setDisplayName(request.getDisplayName());
        if (request.getPhotoUrl() != null) user.setPhotoUrl(request.getPhotoUrl());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDarkMode() != null) user.setDarkMode(request.getDarkMode());
        if (request.getLocationCity() != null) user.setLocationCity(request.getLocationCity());
        if (request.getLocationState() != null) user.setLocationState(request.getLocationState());
        if (request.getLocationCountry() != null) user.setLocationCountry(request.getLocationCountry());
        if (request.getLocationLat() != null) user.setLocationLat(request.getLocationLat());
        if (request.getLocationLng() != null) user.setLocationLng(request.getLocationLng());
        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public void deleteAccount(User user) {
        userRepository.delete(user);
    }

    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToDto(user);
    }

    public static UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .photoUrl(user.getPhotoUrl())
                .phone(user.getPhone())
                .darkMode(user.isDarkMode())
                .locationCity(user.getLocationCity())
                .locationState(user.getLocationState())
                .locationCountry(user.getLocationCountry())
                .locationLat(user.getLocationLat())
                .locationLng(user.getLocationLng())
                .roles(user.getRoles())
                .isArtist(user.isArtist())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

