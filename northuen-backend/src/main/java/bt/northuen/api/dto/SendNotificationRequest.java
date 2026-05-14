package bt.northuen.api.dto;

import bt.northuen.api.entity.Role;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SendNotificationRequest(
        UUID userId,
        Role role,
        boolean broadcast,
        @NotBlank String title,
        @NotBlank String message,
        String type,
        Integer priority
) {}
