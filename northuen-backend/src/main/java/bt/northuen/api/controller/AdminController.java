package bt.northuen.api.controller;

import bt.northuen.api.dto.*;
import bt.northuen.api.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import bt.northuen.api.entity.SettlementStatus;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final bt.northuen.api.service.NotificationService notificationService;

    @GetMapping("/orders")
    public List<OrderResponse> orders() {
        return adminService.orders();
    }

    @PatchMapping("/orders/{id}/assign-driver")
    public OrderResponse assignDriver(@PathVariable UUID id, @Valid @RequestBody AssignDriverRequest request) {
        return adminService.assignDriver(id, request);
    }

    @GetMapping("/cash-report")
    public CashReportResponse cashReport() {
        return adminService.cashReport();
    }

    @GetMapping("/settlements")
    public List<SettlementResponse> settlements(@RequestParam(required = false) SettlementStatus status) {
        return adminService.settlements(status);
    }

    @PatchMapping("/settlements/{id}/mark-paid")
    public SettlementResponse markSettlementPaid(@PathVariable UUID id, @RequestBody MarkSettlementPaidRequest request) {
        return adminService.markSettlementPaid(id, request);
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return adminService.users();
    }

    @GetMapping("/vendors")
    public List<VendorResponse> vendors() {
        return adminService.vendors();
    }

    @GetMapping("/drivers")
    public List<DriverResponse> drivers() {
        return adminService.drivers();
    }

    @GetMapping("/drivers/available")
    public List<DriverResponse> availableDrivers() {
        return adminService.availableDrivers();
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return adminService.dashboard();
    }

    @PatchMapping("/users/{id}/active")
    public UserResponse setActive(@PathVariable UUID id, @Valid @RequestBody UserActiveRequest request) {
        return adminService.setActive(id, request);
    }

    @PostMapping("/notifications")
    public AdminNotificationResponse sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        var count = notificationService.sendFromAdmin(bt.northuen.api.security.CurrentUser.get(), request);
        return new AdminNotificationResponse(count, notificationService.targetLabel(request), request.title());
    }
}
