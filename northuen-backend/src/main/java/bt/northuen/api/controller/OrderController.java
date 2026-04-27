package bt.northuen.api.controller;

import bt.northuen.api.dto.*;
import bt.northuen.api.security.CurrentUser;
import bt.northuen.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(CurrentUser.get(), request);
    }

    @GetMapping("/my")
    public List<OrderResponse> mine() {
        return orderService.mine(CurrentUser.get());
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return orderService.get(CurrentUser.get(), id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(CurrentUser.get(), id, request);
    }

    @PostMapping("/{id}/review")
    public OrderResponse review(@PathVariable UUID id, @Valid @RequestBody ReviewRequest request) {
        return orderService.review(CurrentUser.get(), id, request);
    }

    @GetMapping("/{id}/tracking")
    public List<TrackingPointResponse> tracking(@PathVariable UUID id) {
        return orderService.tracking(CurrentUser.get(), id);
    }
}
