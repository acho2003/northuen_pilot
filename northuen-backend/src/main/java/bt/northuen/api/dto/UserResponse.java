package bt.northuen.api.dto;

import bt.northuen.api.entity.Role;

import java.util.UUID;

public record UserResponse(UUID id, String fullName, String email, String phone, Role role, boolean active) {
}
