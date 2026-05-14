package bt.northuen.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DriverLiveLocationResponse(
        UUID driverId,
        UUID orderId,
        BigDecimal lat,
        BigDecimal lng,
        BigDecimal heading,
        BigDecimal speed,
        LocalDateTime updatedAt
) {
}
