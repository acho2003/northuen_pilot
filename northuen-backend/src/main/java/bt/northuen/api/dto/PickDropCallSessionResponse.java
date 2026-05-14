package bt.northuen.api.dto;

import bt.northuen.api.entity.PickDropCallStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PickDropCallSessionResponse(
        UUID id,
        UUID orderId,
        UUID callerId,
        String callerName,
        UUID receiverId,
        String receiverName,
        PickDropCallStatus status,
        LocalDateTime createdAt,
        LocalDateTime endedAt
) {
}
