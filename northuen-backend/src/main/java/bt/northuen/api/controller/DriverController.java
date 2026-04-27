package bt.northuen.api.controller;

import bt.northuen.api.dto.*;
import bt.northuen.api.security.CurrentUser;
import bt.northuen.api.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DriverController {
    private final DriverService driverService;

    @GetMapping("/api/drivers/assigned-orders")
    public List<OrderResponse> assignedOrders() {
        return driverService.assignedOrders(CurrentUser.get());
    }

    @GetMapping("/api/drivers/available-orders")
    public List<OrderResponse> availableOrders() {
        return driverService.availableOrders(CurrentUser.get());
    }

    @PatchMapping("/api/drivers/available-orders/{deliveryId}/accept")
    public OrderResponse acceptAvailable(@PathVariable UUID deliveryId) {
        return driverService.acceptAvailable(CurrentUser.get(), deliveryId);
    }

    @PatchMapping("/api/drivers/availability")
    public DriverAvailabilityRequest availability(@Valid @RequestBody DriverAvailabilityRequest request) {
        return driverService.availability(CurrentUser.get(), request);
    }

    @PatchMapping("/api/deliveries/{id}/status")
    public DeliveryResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return driverService.updateStatus(CurrentUser.get(), id, request);
    }

    @PostMapping("/api/deliveries/{id}/location")
    public void location(@PathVariable UUID id, @Valid @RequestBody LocationRequest request) {
        driverService.location(CurrentUser.get(), id, request);
    }

    @PatchMapping("/api/deliveries/{id}/complete")
    public DeliveryResponse complete(@PathVariable UUID id) {
        return driverService.complete(CurrentUser.get(), id);
    }
}
