package bt.northuen.api.service;

import bt.northuen.api.dto.*;
import bt.northuen.api.entity.*;
import bt.northuen.api.exception.BusinessRuleException;
import bt.northuen.api.exception.ResourceNotFoundException;
import bt.northuen.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverRepository driverRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryTrackingRepository trackingRepository;
    private final PaymentRepository paymentRepository;
    private final DriverCashSettlementRepository settlementRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final DtoMapper mapper;
    private final GoogleRoadsService googleRoadsService;

    @Transactional(readOnly = true)
    public List<OrderResponse> assignedOrders(User user) {
        var driver = driverRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Driver profile not found."));
        return deliveryRepository.findByDriverAndStatusInOrderByCreatedAtDesc(
                        driver,
                        List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.ACCEPTED, DeliveryStatus.PICKED_UP, DeliveryStatus.ON_THE_WAY)
                ).stream()
                .map(delivery -> mapper.order(delivery.getOrder(), delivery))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> availableOrders(User user) {
        driverRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Driver profile not found."));
        return deliveryRepository.findByDriverIsNullOrderByCreatedAtDesc().stream()
                .filter(delivery -> isAvailableForDriver(delivery.getOrder()))
                .map(delivery -> mapper.order(delivery.getOrder(), delivery))
                .toList();
    }

    @Transactional
    public OrderResponse acceptAvailable(User user, UUID deliveryId) {
        var driver = driverRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Driver profile not found."));
        var delivery = deliveryRepository.findById(deliveryId).orElseThrow(() -> new ResourceNotFoundException("Delivery not found."));
        if (delivery.getDriver() != null) {
            throw new BusinessRuleException("This delivery was already accepted by another driver.");
        }
        if (!isAvailableForDriver(delivery.getOrder())) {
            throw new BusinessRuleException("This delivery is not ready for driver acceptance.");
        }
        delivery.setDriver(driver);
        delivery.setStatus(DeliveryStatus.ACCEPTED);
        driver.setAvailable(false);
        var order = delivery.getOrder();
        order.setStatus(OrderStatus.ACCEPTED);
        driverRepository.save(driver);
        orderRepository.save(order);
        deliveryRepository.save(delivery);
        notificationService.send(order.getCustomer(), "Driver assigned", driver.getUser().getFullName() + " accepted your delivery.");
        return mapper.order(order, delivery);
    }

    @Transactional
    public DriverAvailabilityRequest availability(User user, DriverAvailabilityRequest request) {
        var driver = driverRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Driver profile not found."));
        driver.setAvailable(request.available());
        driverRepository.save(driver);
        return request;
    }

    @Transactional
    public DeliveryResponse updateStatus(User user, UUID deliveryId, UpdateDeliveryStatusRequest request) {
        var delivery = ownedDelivery(user, deliveryId);
        var status = request.status();
        if (status == DeliveryStatus.DELIVERED) {
            throw new BusinessRuleException("Use Mark Delivered & Payment Collected to complete a delivery.");
        }
        delivery.setStatus(status);
        var order = delivery.getOrder();
        if (status == DeliveryStatus.ACCEPTED) {
            order.setStatus(OrderStatus.ACCEPTED);
        } else if (status == DeliveryStatus.PICKED_UP) {
            order.setStatus(OrderStatus.PICKED_UP);
            delivery.setPickedUpAt(LocalDateTime.now());
        } else if (status == DeliveryStatus.ON_THE_WAY) {
            order.setStatus(OrderStatus.ON_THE_WAY);
        }
        orderRepository.save(order);
        notificationService.send(order.getCustomer(), "Delivery updated", "Your driver marked the delivery " + status.name().replace("_", " ") + ".");
        return mapper.delivery(deliveryRepository.save(delivery));
    }

    @Transactional
    public void location(User user, UUID deliveryId, LocationRequest request) {
        var delivery = ownedDelivery(user, deliveryId);
        var driver = delivery.getDriver();
        var previous = trackingRepository.findFirstByDeliveryOrderByCreatedAtDesc(delivery);
        var roadPoint = previous
                .map(point -> googleRoadsService.snap(point.getLatitude(), point.getLongitude(), request.latitude(), request.longitude()))
                .orElseGet(() -> googleRoadsService.snap(request.latitude(), request.longitude()));
        driver.setCurrentLatitude(roadPoint.lat());
        driver.setCurrentLongitude(roadPoint.lng());
        driverRepository.save(driver);

        var tracking = new DeliveryTracking();
        tracking.setDelivery(delivery);
        tracking.setLatitude(roadPoint.lat());
        tracking.setLongitude(roadPoint.lng());
        trackingRepository.save(tracking);
    }

    @Transactional
    public DeliveryResponse complete(User user, UUID deliveryId) {
        var delivery = ownedDelivery(user, deliveryId);
        var order = delivery.getOrder();
        var payment = paymentRepository.findByOrderId(order.getId()).orElseThrow(() -> new ResourceNotFoundException("Payment record not found."));
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(PaymentStatus.PAID);
        payment.setStatus(PaymentStatus.PAID);
        payment.setCollectedAt(LocalDateTime.now());
        payment.setCollectedByDriver(delivery.getDriver());
        delivery.getDriver().setAvailable(true);
        paymentRepository.save(payment);
        settlementRepository.findByPaymentId(payment.getId()).orElseGet(() -> {
            var settlement = new DriverCashSettlement();
            settlement.setDriver(delivery.getDriver());
            settlement.setPayment(payment);
            settlement.setAmount(payment.getAmount());
            var saved = settlementRepository.save(settlement);
            payment.setSettlement(saved);
            paymentRepository.save(payment);
            return saved;
        });
        orderRepository.save(order);
        notificationService.send(order.getCustomer(), "Delivered", "Your order was delivered and COD payment was collected.");
        return mapper.delivery(deliveryRepository.save(delivery));
    }

    private Delivery ownedDelivery(User user, UUID deliveryId) {
        var driver = driverRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Driver profile not found."));
        var delivery = deliveryRepository.findById(deliveryId).orElseThrow(() -> new ResourceNotFoundException("Delivery not found."));
        if (delivery.getDriver() == null || !delivery.getDriver().getId().equals(driver.getId())) {
            throw new BusinessRuleException("Delivery is not assigned to this driver.");
        }
        return delivery;
    }

    private boolean isAvailableForDriver(Order order) {
        if (order.getPaymentStatus() == PaymentStatus.PAID || order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.VENDOR_REJECTED) {
            return false;
        }
        if (order.getOrderType() == OrderType.PARCEL) {
            return order.getStatus() == OrderStatus.PLACED || order.getStatus() == OrderStatus.VENDOR_ACCEPTED || order.getStatus() == OrderStatus.READY_FOR_PICKUP;
        }
        return order.getStatus() == OrderStatus.VENDOR_ACCEPTED || order.getStatus() == OrderStatus.READY_FOR_PICKUP;
    }
}
