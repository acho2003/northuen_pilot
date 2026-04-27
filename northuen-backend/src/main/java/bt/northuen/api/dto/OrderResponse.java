package bt.northuen.api.dto;

import bt.northuen.api.entity.OrderStatus;
import bt.northuen.api.entity.OrderType;
import bt.northuen.api.entity.PaymentStatus;
import bt.northuen.api.entity.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        UUID vendorId,
        OrderType orderType,
        OrderStatus status,
        PaymentType paymentType,
        PaymentStatus paymentStatus,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal totalAmount,
        String pickupAddress,
        String dropoffAddress,
        String parcelDescription,
        String notes,
        List<OrderItemResponse> items,
        DeliveryResponse delivery,
        LocalDateTime createdAt
) {
}
