package bt.northuen.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PickDropCallSignalResponse(
        UUID id,
        UUID callId,
        UUID senderId,
        String type,
        String payload,
        LocalDateTime createdAt
) {
}
