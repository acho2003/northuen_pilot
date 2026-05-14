package bt.northuen.api.controller;

import bt.northuen.api.dto.TrackingRouteRequest;
import bt.northuen.api.dto.TrackingRouteResponse;
import bt.northuen.api.service.TrackingRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tracking")
public class TrackingController {
    private final TrackingRouteService trackingRouteService;

    @PostMapping("/routes")
    public TrackingRouteResponse route(@Valid @RequestBody TrackingRouteRequest request) {
        return trackingRouteService.route(request);
    }
}
