package bt.northuen.api.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RouteLatLngRequest(
        @NotNull BigDecimal lat,
        @NotNull BigDecimal lng
) {
}
