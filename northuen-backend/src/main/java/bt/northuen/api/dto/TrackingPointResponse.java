package bt.northuen.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TrackingPointResponse(UUID id, BigDecimal latitude, BigDecimal longitude, LocalDateTime createdAt) {
}
