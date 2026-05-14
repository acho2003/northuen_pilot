package bt.northuen.api.dto;

import java.math.BigDecimal;

public record PickDropFareResponse(BigDecimal distanceKm, BigDecimal baseFare, BigDecimal perKmRate, BigDecimal estimatedPrice) {
}
