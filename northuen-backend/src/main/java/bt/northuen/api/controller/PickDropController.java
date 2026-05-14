package bt.northuen.api.controller;

import bt.northuen.api.dto.*;
import bt.northuen.api.security.CurrentUser;
import bt.northuen.api.service.PickDropService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pickdrop")
public class PickDropController {
    private final PickDropService pickDropService;

    @PostMapping("/fare")
    public PickDropFareResponse fare(@Valid @RequestBody PickDropFareRequest request) {
        return pickDropService.fare(request);
    }

    @PostMapping("/orders")
    public PickDropOrderResponse create(@Valid @RequestBody CreatePickDropOrderRequest request) {
        return pickDropService.create(CurrentUser.get(), request);
    }

    @GetMapping("/orders/my")
    public List<PickDropOrderResponse> mine() {
        return pickDropService.customerOrders(CurrentUser.get());
    }

    @GetMapping("/orders/{id}")
    public PickDropOrderResponse get(@PathVariable UUID id) {
        return pickDropService.customerOrder(CurrentUser.get(), id);
    }

    @GetMapping("/orders/{id}/live-location")
    public DriverLiveLocationResponse latestLocation(@PathVariable UUID id) {
        return pickDropService.latestLocation(CurrentUser.get(), id);
    }

    @GetMapping("/orders/{id}/messages")
    public List<PickDropMessageResponse> messages(@PathVariable UUID id) {
        return pickDropService.messages(CurrentUser.get(), id);
    }

    @PostMapping("/orders/{id}/messages")
    public PickDropMessageResponse sendMessage(@PathVariable UUID id, @Valid @RequestBody PickDropMessageRequest request) {
        return pickDropService.sendMessage(CurrentUser.get(), id, request);
    }

    @GetMapping("/orders/{id}/contact")
    public PickDropContactResponse contact(@PathVariable UUID id) {
        return pickDropService.contact(CurrentUser.get(), id);
    }

    @PostMapping("/orders/{id}/calls")
    public PickDropCallSessionResponse startCall(@PathVariable UUID id) {
        return pickDropService.startCall(CurrentUser.get(), id);
    }

    @GetMapping("/orders/{id}/calls/active")
    public PickDropCallSessionResponse activeCall(@PathVariable UUID id) {
        return pickDropService.activeCall(CurrentUser.get(), id);
    }

    @GetMapping("/calls/incoming")
    public IncomingPickDropCallResponse incomingCall() {
        return pickDropService.incomingCall(CurrentUser.get());
    }

    @PatchMapping("/calls/{id}/end")
    public PickDropCallSessionResponse endCall(@PathVariable UUID id) {
        return pickDropService.endCall(CurrentUser.get(), id);
    }

    @GetMapping("/calls/{id}/signals")
    public List<PickDropCallSignalResponse> callSignals(@PathVariable UUID id) {
        return pickDropService.callSignals(CurrentUser.get(), id);
    }

    @PostMapping("/calls/{id}/signals")
    public PickDropCallSignalResponse sendCallSignal(@PathVariable UUID id, @Valid @RequestBody PickDropCallSignalRequest request) {
        return pickDropService.sendCallSignal(CurrentUser.get(), id, request);
    }

    @GetMapping("/driver/available")
    public List<PickDropOrderResponse> availableForDriver() {
        return pickDropService.availableForDriver(CurrentUser.get());
    }

    @GetMapping("/driver/mine")
    public List<PickDropOrderResponse> driverOrders() {
        return pickDropService.driverOrders(CurrentUser.get());
    }

    @PatchMapping("/driver/orders/{id}/accept")
    public PickDropOrderResponse accept(@PathVariable UUID id) {
        return pickDropService.accept(CurrentUser.get(), id);
    }

    @PatchMapping("/driver/orders/{id}/reject")
    public PickDropOrderResponse reject(@PathVariable UUID id) {
        return pickDropService.reject(CurrentUser.get(), id);
    }

    @PatchMapping("/driver/orders/{id}/status")
    public PickDropOrderResponse status(@PathVariable UUID id, @Valid @RequestBody PickDropStatusRequest request) {
        return pickDropService.updateStatus(CurrentUser.get(), id, request);
    }

    @PostMapping("/driver/orders/{id}/location")
    public DriverLiveLocationResponse location(@PathVariable UUID id, @Valid @RequestBody DriverLiveLocationRequest request) {
        return pickDropService.location(CurrentUser.get(), id, request);
    }

    @PatchMapping("/driver/orders/{id}/complete")
    public PickDropOrderResponse complete(@PathVariable UUID id) {
        return pickDropService.complete(CurrentUser.get(), id);
    }
}
