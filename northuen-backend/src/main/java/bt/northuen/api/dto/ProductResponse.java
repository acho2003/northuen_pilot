package bt.northuen.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID vendorId,
        String name,
        String description,
        BigDecimal price,
        String category,
        String imageUrl,
        boolean available
) {
}
