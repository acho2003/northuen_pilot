package bt.northuen.api.service;

import bt.northuen.api.dto.*;
import bt.northuen.api.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DtoMapper {
    public UserResponse user(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole(), user.isActive());
    }

    public VendorResponse vendor(Vendor vendor) {
        return new VendorResponse(
                vendor.getId(),
                vendor.getOwner().getId(),
                vendor.getName(),
                vendor.getCategory(),
                vendor.getDescription(),
                vendor.getAddress(),
                vendor.getLatitude(),
                vendor.getLongitude(),
                vendor.getImageUrl(),
                vendor.isOpen()
        );
    }

    public ProductResponse product(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getVendor().getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl(),
                product.isAvailable()
        );
    }

    public OrderResponse order(Order order, Delivery delivery) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getId(), item.getProduct().getId(), item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal()))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getVendor() == null ? null : order.getVendor().getId(),
                order.getOrderType(),
                order.getStatus(),
                order.getPaymentType(),
                order.getPaymentStatus(),
                order.getSubtotal(),
                order.getDeliveryFee(),
                order.getTotalAmount(),
                order.getPickupAddress(),
                order.getDropoffAddress(),
                order.getParcelDescription(),
                order.getNotes(),
                items,
                delivery == null ? null : delivery(delivery),
                order.getCreatedAt()
        );
    }

    public DeliveryResponse delivery(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getOrder().getId(),
                delivery.getDriver() == null ? null : delivery.getDriver().getId(),
                delivery.getStatus(),
                delivery.getPickedUpAt(),
                delivery.getDeliveredAt()
        );
    }

    public DriverResponse driver(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getUser().getId(),
                driver.getUser().getFullName(),
                driver.getUser().getPhone(),
                driver.getVehicleType(),
                driver.getLicenseNumber(),
                driver.isAvailable(),
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude()
        );
    }

    public NotificationResponse notification(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getTitle(), notification.getMessage(), notification.isRead(), notification.getCreatedAt());
    }

    public TrackingPointResponse tracking(DeliveryTracking tracking) {
        return new TrackingPointResponse(tracking.getId(), tracking.getLatitude(), tracking.getLongitude(), tracking.getCreatedAt());
    }

    public CartResponse cart(Cart cart) {
        var items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();
        var subtotal = items.stream().map(CartItemResponse::lineTotal).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        var deliveryFee = items.isEmpty() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal("80.00");
        return new CartResponse(
                cart.getId(),
                cart.getVendor() == null ? null : cart.getVendor().getId(),
                cart.getVendor() == null ? null : cart.getVendor().getName(),
                items,
                subtotal,
                deliveryFee,
                subtotal.add(deliveryFee)
        );
    }

    public SettlementResponse settlement(DriverCashSettlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getDriver().getId(),
                settlement.getDriver().getUser().getFullName(),
                settlement.getPayment().getId(),
                settlement.getPayment().getOrder().getId(),
                settlement.getAmount(),
                settlement.getStatus(),
                settlement.getSettledAt(),
                settlement.getCreatedAt()
        );
    }
}
