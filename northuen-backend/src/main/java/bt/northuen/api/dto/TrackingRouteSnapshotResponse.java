package bt.northuen.api.dto;

import java.time.Instant;
import java.util.List;

public record TrackingRouteSnapshotResponse(
        String encodedPolyline,
        List<RouteLatLngRequest> points,
        int distanceMeters,
        int durationSeconds,
        Instant eta,
        List<RouteStepResponse> steps
) {
}
