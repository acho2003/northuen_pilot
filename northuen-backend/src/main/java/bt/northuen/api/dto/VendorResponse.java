package bt.northuen.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VendorResponse(
        UUID id,
        UUID ownerId,
        String name,
        String category,
        String description,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String imageUrl,
        boolean open
) {
}
