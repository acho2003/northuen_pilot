package bt.northuen.api.service;

import bt.northuen.api.dto.*;
import bt.northuen.api.entity.*;
import bt.northuen.api.exception.ResourceNotFoundException;
import bt.northuen.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;
    private final PaymentRepository paymentRepository;
    private final DriverCashSettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final NotificationService notificationService;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<OrderResponse> orders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(order -> mapper.order(order, deliveryRepository.findByOrder(order).orElse(null))).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> users() {
        return userRepository.findAll().stream().map(mapper::user).toList();
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> vendors() {
        return vendorRepository.findAll().stream().map(mapper::vendor).toList();
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> drivers() {
        return driverRepository.findAll().stream().map(mapper::driver).toList();
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> availableDrivers() {
        return driverRepository.findByAvailableTrueOrderByUpdatedAtDesc().stream().map(mapper::driver).toList();
    }

    @Transactional
    public UserResponse setActive(UUID userId, UserActiveRequest request) {
        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setActive(request.active());
        return mapper.user(userRepository.save(user));
    }

    @Transactional
    public OrderResponse assignDriver(UUID orderId, AssignDriverRequest request) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found."));
        var driver = driverRepository.findById(request.driverId()).orElseThrow(() -> new ResourceNotFoundException("Driver not found."));
        var delivery = deliveryRepository.findByOrder(order).orElseThrow(() -> new ResourceNotFoundException("Delivery record not found."));
        delivery.setDriver(driver);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        driver.setAvailable(false);
        order.setStatus(OrderStatus.DRIVER_ASSIGNED);
        driverRepository.save(driver);
        orderRepository.save(order);
        deliveryRepository.save(delivery);
        notificationService.send(driver.getUser(), "Delivery assigned", "A COD delivery has been assigned to you.");
        return mapper.order(order, delivery);
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse dashboard() {
        var orders = orderRepository.findAll();
        long activeOrders = orders.stream()
                .filter(order -> !List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.VENDOR_REJECTED).contains(order.getStatus()))
                .count();
        long deliveredOrders = orders.stream().filter(order -> order.getStatus() == OrderStatus.DELIVERED).count();
        return new AdminDashboardResponse(
                orders.size(),
                activeOrders,
                deliveredOrders,
                driverRepository.findByAvailableTrueOrderByUpdatedAtDesc().size(),
                paymentRepository.totalCollected(),
                settlementRepository.totalPending()
        );
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> settlements(SettlementStatus status) {
        var settlements = status == null ? settlementRepository.findAll() : settlementRepository.findByStatusOrderByCreatedAtDesc(status);
        return settlements.stream().map(mapper::settlement).toList();
    }

    @Transactional
    public SettlementResponse markSettlementPaid(UUID settlementId, MarkSettlementPaidRequest request) {
        var settlement = settlementRepository.findById(settlementId).orElseThrow(() -> new ResourceNotFoundException("Settlement not found."));
        settlement.setStatus(SettlementStatus.PAID);
        settlement.setSettledAt(java.time.LocalDateTime.now());
        settlement.setNotes(request.notes());
        return mapper.settlement(settlementRepository.save(settlement));
    }

    @Transactional(readOnly = true)
    public CashReportResponse cashReport() {
        var rows = new LinkedHashMap<String, BigDecimal[]>();
        for (var payment : paymentRepository.findAll()) {
            var delivery = deliveryRepository.findByOrder(payment.getOrder()).orElse(null);
            Driver driver = payment.getCollectedByDriver() != null ? payment.getCollectedByDriver() : delivery == null ? null : delivery.getDriver();
            String name = driver == null ? "Unassigned" : driver.getUser().getFullName();
            rows.putIfAbsent(name, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (payment.getStatus() == PaymentStatus.PAID) {
                rows.get(name)[0] = rows.get(name)[0].add(payment.getAmount());
            } else {
                rows.get(name)[1] = rows.get(name)[1].add(payment.getAmount());
            }
        }
        var driverRows = rows.entrySet().stream()
                .map(entry -> new CashReportResponse.DriverCashRow(entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
                .toList();
        return new CashReportResponse(
                paymentRepository.totalCollected(),
                paymentRepository.totalPending(),
                settlementRepository.totalPending(),
                settlementRepository.totalPaid(),
                paymentRepository.countByStatus(PaymentStatus.PAID),
                paymentRepository.countByStatus(PaymentStatus.PENDING),
                driverRows
        );
    }
}
