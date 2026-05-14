package bt.northuen.api.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PickDropFareRequest(
        @NotNull BigDecimal pickupLat,
        @NotNull BigDecimal pickupLng,
        @NotNull BigDecimal dropLat,
        @NotNull BigDecimal dropLng
) {
}
