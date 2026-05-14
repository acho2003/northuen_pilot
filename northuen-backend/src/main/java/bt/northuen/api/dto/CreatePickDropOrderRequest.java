package bt.northuen.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePickDropOrderRequest(
        @NotBlank @Size(max = 500) String pickupAddress,
        @NotNull BigDecimal pickupLat,
        @NotNull BigDecimal pickupLng,
        @NotBlank @Size(max = 500) String dropAddress,
        @NotNull BigDecimal dropLat,
        @NotNull BigDecimal dropLng,
        @NotBlank @Size(max = 80) String itemType,
        @NotBlank @Size(max = 1000) String itemDescription
) {
}
