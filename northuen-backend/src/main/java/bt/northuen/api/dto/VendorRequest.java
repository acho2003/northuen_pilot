package bt.northuen.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record VendorRequest(
        @NotBlank String name,
        @NotBlank String category,
        @NotBlank String description,
        @NotBlank String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String imageUrl,
        boolean open
) {
}
