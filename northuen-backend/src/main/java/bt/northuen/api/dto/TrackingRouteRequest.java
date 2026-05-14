package bt.northuen.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrackingRouteRequest(
        @NotBlank String orderId,
        @Valid @NotNull RouteLatLngRequest origin,
        @Valid @NotNull RouteLatLngRequest destination,
        boolean force
) {
}
