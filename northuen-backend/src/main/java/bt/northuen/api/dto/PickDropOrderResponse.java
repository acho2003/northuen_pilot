package bt.northuen.api.dto;

import bt.northuen.api.entity.PaymentStatus;
import bt.northuen.api.entity.PickDropStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PickDropOrderResponse(
        UUID id,
        UUID customerId,
        UUID driverId,
        String driverName,
        String pickupAddress,
        BigDecimal pickupLat,
        BigDecimal pickupLng,
        String dropAddress,
        BigDecimal dropLat,
        BigDecimal dropLng,
        String itemType,
        String itemDescription,
        BigDecimal estimatedDistanceKm,
        BigDecimal estimatedPrice,
        PickDropStatus status,
        PaymentStatus paymentStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
