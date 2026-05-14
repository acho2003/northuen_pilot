package bt.northuen.api.service;

import bt.northuen.api.dto.*;
import bt.northuen.api.entity.*;
import bt.northuen.api.exception.BusinessRuleException;
import bt.northuen.api.exception.ResourceNotFoundException;
import bt.northuen.api.repository.DriverLiveLocationRepository;
import bt.northuen.api.repository.DriverRepository;
import bt.northuen.api.repository.PickDropCallSessionRepository;
import bt.northuen.api.repository.PickDropCallSignalRepository;
import bt.northuen.api.repository.PickDropMessageRepository;
import bt.northuen.api.repository.PickDropOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PickDropService {
    private static final BigDecimal BASE_FARE = new BigDecimal("50.00");
    private static final BigDecimal PER_KM_RATE = new BigDecimal("20.00");

    private final PickDropOrderRepository orderRepository;
    private final PickDropMessageRepository messageRepository;
    private final PickDropCallSessionRepository callSessionRepository;
    private final PickDropCallSignalRepository callSignalRepository;
    private final DriverRepository driverRepository;
    private final DriverLiveLocationRepository liveLocationRepository;
    private final NotificationService notificationService;
    private final GoogleRoadsService googleRoadsService;

    @Transactional(readOnly = true)
    public PickDropFareResponse fare(PickDropFareRequest request) {
        var distance = distanceKm(request.pickupLat(), request.pickupLng(), request.dropLat(), request.dropLng());
        return new PickDropFareResponse(distance, BASE_FARE, PER_KM_RATE, price(distance));
    }

    @Transactional
    public PickDropOrderResponse create(User customer, CreatePickDropOrderRequest request) {
        var distance = distanceKm(request.pickupLat(), request.pickupLng(), request.dropLat(), request.dropLng());
        var order = new PickDropOrder();
        order.setCustomer(customer);
        order.setPickupAddress(request.pickupAddress());
        order.setPickupLat(request.pickupLat());
        order.setPickupLng(request.pickupLng());
        order.setDropAddress(request.dropAddress());
        order.setDropLat(request.dropLat());
        order.setDropLng(request.dropLng());
        order.setItemType(request.itemType());
        order.setItemDescription(request.itemDescription());
        order.setEstimatedDistanceKm(distance);
        order.setEstimatedPrice(price(distance));
        var saved = orderRepository.save(order);
        driverRepository.findByAvailableTrueOrderByUpdatedAtDesc()
                .forEach(driver -> notificationService.send(driver.getUser(), "New Pick & Drop request", "A pickup request is available in your work list."));
        return response(saved);
    }

    @Transactional(readOnly = true)
    public List<PickDropOrderResponse> customerOrders(User customer) {
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public PickDropOrderResponse customerOrder(User customer, UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Pick & Drop order not found."));
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessRuleException("This Pick & Drop order belongs to another customer.");
        }
        return response(order);
    }

    @Transactional(readOnly = true)
    public List<PickDropOrderResponse> availableForDriver(User user) {
        var driver = driver(user);
        var driverAssigned = orderRepository.findByDriverAndStatusInOrderByCreatedAtDesc(driver, List.of(PickDropStatus.DRIVER_ASSIGNED));
        var open = orderRepository.findByDriverIsNullAndStatusInOrderByCreatedAtDesc(List.of(PickDropStatus.PENDING));
        return java.util.stream.Stream.concat(driverAssigned.stream(), open.stream())
                .sorted(Comparator.comparing(PickDropOrder::getCreatedAt).reversed())
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PickDropOrderResponse> driverOrders(User user) {
        return orderRepository.findByDriverAndStatusInOrderByCreatedAtDesc(
                        driver(user),
                        List.of(PickDropStatus.ACCEPTED, PickDropStatus.ARRIVED_PICKUP, PickDropStatus.PICKED_UP, PickDropStatus.ARRIVED_DROP)
                ).stream()
                .map(this::response)
                .toList();
    }

    @Transactional
    public PickDropOrderResponse accept(User user, UUID orderId) {
        var driver = driver(user);
        var order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Pick & Drop order not found."));
        if (order.getDriver() != null && !order.getDriver().getId().equals(driver.getId())) {
            throw new BusinessRuleException("This request is assigned to another driver.");
        }
        if (order.getStatus() != PickDropStatus.PENDING && order.getStatus() != PickDropStatus.DRIVER_ASSIGNED) {
            throw new BusinessRuleException("This request cannot be accepted now.");
        }
        order.setDriver(driver);
        order.setStatus(PickDropStatus.ACCEPTED);
        driver.setAvailable(false);
        driverRepository.save(driver);
        notificationService.send(order.getCustomer(), "Driver accepted", driver.getUser().getFullName() + " accepted your Pick & Drop request.");
        return response(orderRepository.save(order));
    }

    @Transactional
    public PickDropOrderResponse reject(User user, UUID orderId) {
        var driver = driver(user);
        var order = ownedOrAssigned(driver, orderId);
        if (order.getStatus() != PickDropStatus.DRIVER_ASSIGNED && order.getStatus() != PickDropStatus.ACCEPTED) {
            throw new BusinessRuleException("This request cannot be rejected now.");
        }
        order.setDriver(null);
        order.setStatus(PickDropStatus.PENDING);
        driver.setAvailable(true);
        driverRepository.save(driver);
        notificationService.send(order.getCustomer(), "Finding another driver", "Your Pick & Drop request is being offered to nearby drivers.");
        return response(orderRepository.save(order));
    }

    @Transactional
    public PickDropOrderResponse updateStatus(User user, UUID orderId, PickDropStatusRequest request) {
        var driver = driver(user);
        var order = owned(driver, orderId);
        var next = request.status();
        if (next == PickDropStatus.DELIVERED) {
            throw new BusinessRuleException("Use Delivered & Cash Collected to complete Pick & Drop.");
        }
        if (!List.of(PickDropStatus.ARRIVED_PICKUP, PickDropStatus.PICKED_UP, PickDropStatus.ARRIVED_DROP, PickDropStatus.CANCELLED).contains(next)) {
            throw new BusinessRuleException("Unsupported Pick & Drop status update.");
        }
        order.setStatus(next);
        if (next == PickDropStatus.CANCELLED) {
            driver.setAvailable(true);
            driverRepository.save(driver);
        }
        notificationService.send(order.getCustomer(), "Pick & Drop updated", "Driver marked your request " + next.name().replace("_", " ") + ".");
        return response(orderRepository.save(order));
    }

    @Transactional
    public DriverLiveLocationResponse location(User user, UUID orderId, DriverLiveLocationRequest request) {
        var driver = driver(user);
        var order = owned(driver, orderId);
        if (order.getStatus() == PickDropStatus.DELIVERED || order.getStatus() == PickDropStatus.CANCELLED) {
            throw new BusinessRuleException("Live tracking is closed for this request.");
        }
        var location = liveLocationRepository.findByDriverAndOrder(driver, order).orElseGet(() -> {
            var next = new DriverLiveLocation();
            next.setDriver(driver);
            next.setOrder(order);
            return next;
        });
        var roadPoint = location.getLat() == null
                ? googleRoadsService.snap(request.lat(), request.lng())
                : googleRoadsService.snap(location.getLat(), location.getLng(), request.lat(), request.lng());
        driver.setCurrentLatitude(roadPoint.lat());
        driver.setCurrentLongitude(roadPoint.lng());
        driverRepository.save(driver);
        location.setLat(roadPoint.lat());
        location.setLng(roadPoint.lng());
        location.setHeading(request.heading());
        location.setSpeed(request.speed());
        return response(liveLocationRepository.save(location));
    }

    @Transactional(readOnly = true)
    public DriverLiveLocationResponse latestLocation(User customer, UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Pick & Drop order not found."));
        if (!order.getCustomer().getId().equals(customer.getId()) && (order.getDriver() == null || !order.getDriver().getUser().getId().equals(customer.getId()))) {
            throw new BusinessRuleException("You cannot view tracking for this request.");
        }
        return liveLocationRepository.findFirstByOrderOrderByUpdatedAtDesc(order).map(this::response).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PickDropMessageResponse> messages(User user, UUID orderId) {
        var order = chatOrder(user, orderId);
        return messageRepository.findTop80ByOrderOrderByCreatedAtAsc(order).stream().map(this::response).toList();
    }

    @Transactional
    public PickDropMessageResponse sendMessage(User user, UUID orderId, PickDropMessageRequest request) {
        var order = chatOrder(user, orderId);
        var message = new PickDropMessage();
        message.setOrder(order);
        message.setSender(user);
        message.setSenderRole(user.getRole());
        message.setBody(request.body().trim());
        var recipient = user.getId().equals(order.getCustomer().getId()) ? order.getDriver().getUser() : order.getCustomer();
        notificationService.send(recipient, "New Pick & Drop message", user.getFullName() + " sent a message.");
        return response(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public PickDropContactResponse contact(User user, UUID orderId) {
        var order = chatOrder(user, orderId);
        var contact = user.getId().equals(order.getCustomer().getId()) ? order.getDriver().getUser() : order.getCustomer();
        return new PickDropContactResponse(contact.getId(), contact.getFullName(), contact.getPhone());
    }

    @Transactional
    public PickDropCallSessionResponse startCall(User user, UUID orderId) {
        var order = chatOrder(user, orderId);
        var existing = callSessionRepository.findFirstByOrderAndStatusInOrderByCreatedAtDesc(order, List.of(PickDropCallStatus.RINGING, PickDropCallStatus.ACTIVE));
        if (existing.isPresent()) {
            return response(existing.get());
        }
        var receiver = user.getId().equals(order.getCustomer().getId()) ? order.getDriver().getUser() : order.getCustomer();
        var call = new PickDropCallSession();
        call.setOrder(order);
        call.setCaller(user);
        call.setReceiver(receiver);
        notificationService.send(receiver, "Incoming Northuen call", user.getFullName() + " is calling in the app.");
        return response(callSessionRepository.save(call));
    }

    @Transactional(readOnly = true)
    public PickDropCallSessionResponse activeCall(User user, UUID orderId) {
        var order = chatOrder(user, orderId);
        return callSessionRepository.findFirstByOrderAndStatusInOrderByCreatedAtDesc(order, List.of(PickDropCallStatus.RINGING, PickDropCallStatus.ACTIVE))
                .filter(call -> canAccessCall(user, call))
                .map(this::response)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public IncomingPickDropCallResponse incomingCall(User user) {
        return callSessionRepository.findFirstByReceiverAndStatusInOrderByCreatedAtDesc(user, List.of(PickDropCallStatus.RINGING))
                .map(call -> new IncomingPickDropCallResponse(response(call), response(call.getOrder())))
                .orElse(null);
    }

    @Transactional
    public PickDropCallSessionResponse endCall(User user, UUID callId) {
        var call = call(user, callId);
        call.setStatus(PickDropCallStatus.ENDED);
        call.setEndedAt(LocalDateTime.now());
        return response(callSessionRepository.save(call));
    }

    @Transactional(readOnly = true)
    public List<PickDropCallSignalResponse> callSignals(User user, UUID callId) {
        var call = call(user, callId);
        return callSignalRepository.findTop200ByCallOrderByCreatedAtAsc(call).stream().map(this::response).toList();
    }

    @Transactional
    public PickDropCallSignalResponse sendCallSignal(User user, UUID callId, PickDropCallSignalRequest request) {
        var call = call(user, callId);
        if ("answer".equalsIgnoreCase(request.type()) && call.getStatus() == PickDropCallStatus.RINGING) {
            call.setStatus(PickDropCallStatus.ACTIVE);
            callSessionRepository.save(call);
        }
        var signal = new PickDropCallSignal();
        signal.setCall(call);
        signal.setSender(user);
        signal.setType(request.type());
        signal.setPayload(request.payload());
        return response(callSignalRepository.save(signal));
    }

    @Transactional
    public PickDropOrderResponse complete(User user, UUID orderId) {
        var driver = driver(user);
        var order = owned(driver, orderId);
        if (order.getStatus() != PickDropStatus.ARRIVED_DROP && order.getStatus() != PickDropStatus.PICKED_UP) {
            throw new BusinessRuleException("Driver must pick up the item before delivery can be completed.");
        }
        order.setStatus(PickDropStatus.DELIVERED);
        order.setPaymentStatus(PaymentStatus.PAID);
        driver.setAvailable(true);
        driverRepository.save(driver);
        notificationService.send(order.getCustomer(), "Pick & Drop delivered", "Your item was delivered and cash was collected.");
        return response(orderRepository.save(order));
    }

    private Driver driver(User user) {
        return driverRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException("Driver profile not found."));
    }

    private PickDropOrder owned(Driver driver, UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Pick & Drop order not found."));
        if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId())) {
            throw new BusinessRuleException("This Pick & Drop request is not assigned to this driver.");
        }
        return order;
    }

    private PickDropOrder ownedOrAssigned(Driver driver, UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Pick & Drop order not found."));
        if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId())) {
            throw new BusinessRuleException("This Pick & Drop request is not assigned to this driver.");
        }
        return order;
    }

    private PickDropOrder chatOrder(User user, UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Pick & Drop order not found."));
        if (order.getDriver() == null || order.getStatus() == PickDropStatus.PENDING || order.getStatus() == PickDropStatus.DRIVER_ASSIGNED) {
            throw new BusinessRuleException("Chat opens after a driver accepts this Pick & Drop request.");
        }
        var isCustomer = order.getCustomer().getId().equals(user.getId());
        var isDriver = order.getDriver().getUser().getId().equals(user.getId());
        if (!isCustomer && !isDriver) {
            throw new BusinessRuleException("You cannot chat on this Pick & Drop request.");
        }
        return order;
    }

    private PickDropCallSession call(User user, UUID callId) {
        var call = callSessionRepository.findById(callId).orElseThrow(() -> new ResourceNotFoundException("Call not found."));
        if (!canAccessCall(user, call)) {
            throw new BusinessRuleException("You cannot access this call.");
        }
        return call;
    }

    private boolean canAccessCall(User user, PickDropCallSession call) {
        return call.getCaller().getId().equals(user.getId()) || call.getReceiver().getId().equals(user.getId());
    }

    private BigDecimal distanceKm(BigDecimal fromLat, BigDecimal fromLng, BigDecimal toLat, BigDecimal toLng) {
        double earthRadiusKm = 6371.0;
        double lat1 = Math.toRadians(fromLat.doubleValue());
        double lat2 = Math.toRadians(toLat.doubleValue());
        double dLat = Math.toRadians(toLat.subtract(fromLat).doubleValue());
        double dLng = Math.toRadians(toLng.subtract(fromLng).doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(earthRadiusKm * c).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal price(BigDecimal distanceKm) {
        return BASE_FARE.add(distanceKm.multiply(PER_KM_RATE)).setScale(2, RoundingMode.HALF_UP);
    }

    private PickDropOrderResponse response(PickDropOrder order) {
        return new PickDropOrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getDriver() == null ? null : order.getDriver().getId(),
                order.getDriver() == null ? null : order.getDriver().getUser().getFullName(),
                order.getPickupAddress(),
                order.getPickupLat(),
                order.getPickupLng(),
                order.getDropAddress(),
                order.getDropLat(),
                order.getDropLng(),
                order.getItemType(),
                order.getItemDescription(),
                order.getEstimatedDistanceKm(),
                order.getEstimatedPrice(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private DriverLiveLocationResponse response(DriverLiveLocation location) {
        return new DriverLiveLocationResponse(
                location.getDriver().getId(),
                location.getOrder().getId(),
                location.getLat(),
                location.getLng(),
                location.getHeading(),
                location.getSpeed(),
                location.getUpdatedAt()
        );
    }

    private PickDropMessageResponse response(PickDropMessage message) {
        return new PickDropMessageResponse(
                message.getId(),
                message.getOrder().getId(),
                message.getSender().getId(),
                message.getSender().getFullName(),
                message.getSenderRole(),
                message.getBody(),
                message.getCreatedAt()
        );
    }

    private PickDropCallSessionResponse response(PickDropCallSession call) {
        return new PickDropCallSessionResponse(
                call.getId(),
                call.getOrder().getId(),
                call.getCaller().getId(),
                call.getCaller().getFullName(),
                call.getReceiver().getId(),
                call.getReceiver().getFullName(),
                call.getStatus(),
                call.getCreatedAt(),
                call.getEndedAt()
        );
    }

    private PickDropCallSignalResponse response(PickDropCallSignal signal) {
        return new PickDropCallSignalResponse(
                signal.getId(),
                signal.getCall().getId(),
                signal.getSender().getId(),
                signal.getType(),
                signal.getPayload(),
                signal.getCreatedAt()
        );
    }
}
