package bt.northuen.api.service;

import bt.northuen.api.dto.CreateOrderRequest;
import bt.northuen.api.dto.OrderResponse;
import bt.northuen.api.dto.ReviewRequest;
import bt.northuen.api.dto.TrackingPointResponse;
import bt.northuen.api.dto.UpdateOrderStatusRequest;
import bt.northuen.api.entity.*;
import bt.northuen.api.exception.BusinessRuleException;
import bt.northuen.api.exception.ResourceNotFoundException;
import bt.northuen.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final BigDecimal DEFAULT_DELIVERY_FEE = new BigDecimal("80.00");

    private final OrderRepository orderRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryTrackingRepository trackingRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final DtoMapper mapper;

    @Transactional
    public OrderResponse create(User customer, CreateOrderRequest request) {
        var order = new Order();
        order.setCustomer(customer);
        order.setOrderType(request.orderType());
        order.setPickupAddress(request.pickupAddress());
        order.setDropoffAddress(request.dropoffAddress());
        order.setParcelDescription(request.parcelDescription());
        order.setNotes(request.notes());
        order.setDeliveryFee(request.deliveryFee() == null ? DEFAULT_DELIVERY_FEE : request.deliveryFee());

        if (request.orderType() == OrderType.PARCEL) {
            order.setSubtotal(BigDecimal.ZERO);
        } else {
            if (request.vendorId() == null || request.items() == null || request.items().isEmpty()) {
                throw new BusinessRuleException("Food and shop orders require a vendor and cart items.");
            }
            var vendor = vendorRepository.findById(request.vendorId()).orElseThrow(() -> new ResourceNotFoundException("Vendor not found."));
            if (!vendor.isOpen()) {
                throw new BusinessRuleException("Vendor is currently closed.");
            }
            order.setVendor(vendor);
            BigDecimal subtotal = BigDecimal.ZERO;
            for (var itemRequest : request.items()) {
                var product = productRepository.findById(itemRequest.productId()).orElseThrow(() -> new ResourceNotFoundException("Product not found."));
                if (!product.isAvailable() || !product.getVendor().getId().equals(vendor.getId())) {
                    throw new BusinessRuleException("Product is not available for this vendor.");
                }
                var item = new OrderItem();
                item.setOrder(order);
                item.setProduct(product);
                item.setProductName(product.getName());
                item.setUnitPrice(product.getPrice());
                item.setQuantity(itemRequest.quantity());
                item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
                order.getItems().add(item);
                subtotal = subtotal.add(item.getLineTotal());
            }
            order.setSubtotal(subtotal);
        }

        order.setTotalAmount(order.getSubtotal().add(order.getDeliveryFee()));
        var saved = orderRepository.save(order);

        var delivery = new Delivery();
        delivery.setOrder(saved);
        deliveryRepository.save(delivery);

        var payment = new Payment();
        payment.setOrder(saved);
        payment.setAmount(saved.getTotalAmount());
        paymentRepository.save(payment);

        if (saved.getVendor() != null) {
            notificationService.send(saved.getVendor().getOwner(), "New order", "A new COD order is waiting for your response.");
        }

        return mapper.order(saved, delivery);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> mine(User user) {
        return orderRepository.findByCustomerOrderByCreatedAtDesc(user).stream().map(this::mapWithDelivery).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(User user, UUID id) {
        var order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found."));
        if (!canViewOrder(user, order)) {
            throw new BusinessRuleException("Order does not belong to you.");
        }
        return mapWithDelivery(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> vendorOrders(User vendorUser) {
        var vendor = vendorRepository.findByOwner(vendorUser).orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found."));
        return orderRepository.findByVendorOrderByCreatedAtDesc(vendor).stream().map(this::mapWithDelivery).toList();
    }

    @Transactional(readOnly = true)
    public List<TrackingPointResponse> tracking(User user, UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found."));
        if (!canViewOrder(user, order)) {
            throw new BusinessRuleException("Order does not belong to you.");
        }
        var delivery = deliveryRepository.findByOrder(order).orElseThrow(() -> new ResourceNotFoundException("Delivery record not found."));
        return trackingRepository.findTop20ByDeliveryOrderByCreatedAtDesc(delivery).stream().map(mapper::tracking).toList();
    }

    @Transactional
    public OrderResponse updateStatus(User actor, UUID id, UpdateOrderStatusRequest request) {
        var order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found."));
        var next = request.status();
        if (next == OrderStatus.DELIVERED && order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessRuleException("Use the delivery completion endpoint so delivered and paid are updated together.");
        }

        if (actor.getRole() == Role.VENDOR) {
            var vendor = vendorRepository.findByOwner(actor).orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found."));
            if (order.getVendor() == null || !order.getVendor().getId().equals(vendor.getId())) {
                throw new BusinessRuleException("Order does not belong to your vendor profile.");
            }
            if (!List.of(OrderStatus.VENDOR_ACCEPTED, OrderStatus.VENDOR_REJECTED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP).contains(next)) {
                throw new BusinessRuleException("Vendors can only accept, reject, or update preparation status.");
            }
        } else if (actor.getRole() == Role.CUSTOMER) {
            if (!order.getCustomer().getId().equals(actor.getId()) || next != OrderStatus.CANCELLED) {
                throw new BusinessRuleException("Customers can only cancel their own order.");
            }
        } else if (actor.getRole() != Role.ADMIN) {
            throw new BusinessRuleException("Use delivery endpoints for driver status updates.");
        }

        order.setStatus(next);
        var updated = orderRepository.save(order);
        notificationService.send(order.getCustomer(), "Order updated", "Your order is now " + next.name().replace("_", " ") + ".");
        return mapWithDelivery(updated);
    }

    @Transactional
    public OrderResponse review(User customer, UUID orderId, ReviewRequest request) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found."));
        if (!order.getCustomer().getId().equals(customer.getId()) || order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessRuleException("Only delivered orders can be reviewed by the ordering customer.");
        }
        var delivery = deliveryRepository.findByOrder(order).orElse(null);
        var review = new Review();
        review.setOrder(order);
        review.setCustomer(customer);
        review.setVendor(order.getVendor());
        review.setDriver(delivery == null ? null : delivery.getDriver());
        review.setVendorRating(request.vendorRating());
        review.setDriverRating(request.driverRating());
        review.setComment(request.comment());
        reviewRepository.save(review);
        return mapWithDelivery(order);
    }

    private OrderResponse mapWithDelivery(Order order) {
        return mapper.order(order, deliveryRepository.findByOrder(order).orElse(null));
    }

    private boolean canViewOrder(User user, Order order) {
        if (user.getRole() == Role.ADMIN || order.getCustomer().getId().equals(user.getId())) {
            return true;
        }
        if (user.getRole() == Role.VENDOR && order.getVendor() != null) {
            return vendorRepository.findByOwner(user)
                    .map(vendor -> vendor.getId().equals(order.getVendor().getId()))
                    .orElse(false);
        }
        if (user.getRole() == Role.DRIVER) {
            return deliveryRepository.findByOrder(order)
                    .map(delivery -> delivery.getDriver() != null && delivery.getDriver().getUser().getId().equals(user.getId()))
                    .orElse(false);
        }
        return false;
    }
}
