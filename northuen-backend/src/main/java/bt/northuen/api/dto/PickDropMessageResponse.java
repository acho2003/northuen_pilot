package bt.northuen.api.dto;

import bt.northuen.api.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record PickDropMessageResponse(
        UUID id,
        UUID orderId,
        UUID senderId,
        String senderName,
        Role senderRole,
        String body,
        LocalDateTime createdAt
) {
}
