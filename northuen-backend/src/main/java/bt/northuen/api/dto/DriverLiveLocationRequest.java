package bt.northuen.api.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DriverLiveLocationRequest(
        @NotNull BigDecimal lat,
        @NotNull BigDecimal lng,
        BigDecimal heading,
        BigDecimal speed
) {
}
