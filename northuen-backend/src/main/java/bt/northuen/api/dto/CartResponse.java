package bt.northuen.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(UUID id, UUID vendorId, String vendorName, List<CartItemResponse> items, BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal total) {
}
