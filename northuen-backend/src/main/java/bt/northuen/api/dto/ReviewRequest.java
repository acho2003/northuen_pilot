package bt.northuen.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReviewRequest(
        @Min(1) @Max(5) Integer vendorRating,
        @Min(1) @Max(5) Integer driverRating,
        String comment
) {
}
