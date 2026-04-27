package bt.northuen.api.dto;

import bt.northuen.api.entity.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryResponse(UUID id, UUID orderId, UUID driverId, DeliveryStatus status, LocalDateTime pickedUpAt, LocalDateTime deliveredAt) {
}
