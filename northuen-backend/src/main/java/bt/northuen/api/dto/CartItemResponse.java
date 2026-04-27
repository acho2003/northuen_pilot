package bt.northuen.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(UUID id, UUID productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
