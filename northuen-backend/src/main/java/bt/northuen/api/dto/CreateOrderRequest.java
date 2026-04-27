package bt.northuen.api.dto;

import bt.northuen.api.entity.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull OrderType orderType,
        UUID vendorId,
        @Valid List<CartItemRequest> items,
        @NotBlank String pickupAddress,
        @NotBlank String dropoffAddress,
        String parcelDescription,
        String notes,
        BigDecimal deliveryFee
) {
}
