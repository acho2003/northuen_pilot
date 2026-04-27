package bt.northuen.api.dto;

import jakarta.validation.constraints.NotNull;

public record DriverAvailabilityRequest(@NotNull Boolean available) {
}
