package bt.northuen.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String title,
        String message,
        boolean read,
        String type,
        String targetRole,
        Integer priority,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
