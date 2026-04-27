package bt.northuen.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(UUID id, String title, String message, boolean read, LocalDateTime createdAt) {
}
