package bt.northuen.api.dto;

import bt.northuen.api.entity.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        UUID driverId,
        String driverName,
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        SettlementStatus status,
        LocalDateTime settledAt,
        LocalDateTime createdAt
) {
}
