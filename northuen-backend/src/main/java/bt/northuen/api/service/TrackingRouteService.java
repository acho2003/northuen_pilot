package bt.northuen.api.service;

import bt.northuen.api.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrackingRouteService {
    private final ObjectMapper objectMapper;

    @Value("${app.google.server-api-key:}")
    private String googleServerApiKey;

    public TrackingRouteResponse route(TrackingRouteRequest request) {
        if (googleServerApiKey == null || googleServerApiKey.isBlank()) {
            return fallback(request);
        }

        var body = Map.of(
                "origin", waypoint(request.origin()),
                "destination", waypoint(request.destination()),
                "travelMode", "DRIVE",
                "routingPreference", "TRAFFIC_AWARE",
                "computeAlternativeRoutes", false,
                "polylineQuality", "HIGH_QUALITY",
                "polylineEncoding", "ENCODED_POLYLINE"
        );

        try {
            var json = RestClient.create("https://routes.googleapis.com")
                    .post()
                    .uri("/directions/v2:computeRoutes")
                    .header("X-Goog-Api-Key", googleServerApiKey)
                    .header("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.legs.steps.distanceMeters,routes.legs.steps.staticDuration,routes.legs.steps.navigationInstruction")
                    .body(body)
                    .retrieve()
                    .body(String.class);
            var root = objectMapper.readTree(json);
            var routes = root.path("routes");
            if (!routes.isArray() || routes.isEmpty()) {
                return fallback(request);
            }
            return new TrackingRouteResponse(snapshotFrom(routes.get(0), request));
        } catch (Exception ignored) {
            return fallback(request);
        }
    }

    private TrackingRouteSnapshotResponse snapshotFrom(JsonNode route, TrackingRouteRequest request) {
        var durationSeconds = parseDuration(route.path("duration").asText());
        var distanceMeters = route.path("distanceMeters").asInt();
        var encodedPolyline = route.path("polyline").path("encodedPolyline").asText("");
        var steps = new ArrayList<RouteStepResponse>();

        for (var leg : route.path("legs")) {
            for (var step : leg.path("steps")) {
                var instruction = step.path("navigationInstruction").path("instructions").asText("Continue");
                var maneuver = step.path("navigationInstruction").path("maneuver").asText("continue");
                steps.add(new RouteStepResponse(
                        instruction,
                        maneuver,
                        step.path("distanceMeters").asInt(),
                        parseDuration(step.path("staticDuration").asText())
                ));
            }
        }

        return new TrackingRouteSnapshotResponse(
                encodedPolyline,
                List.of(request.origin(), request.destination()),
                distanceMeters,
                durationSeconds,
                Instant.now().plusSeconds(Math.max(durationSeconds, 0)),
                steps
        );
    }

    private TrackingRouteResponse fallback(TrackingRouteRequest request) {
        var distance = distanceMeters(request.origin(), request.destination());
        var duration = distance == 0 ? 0 : Math.max(60, distance / 8);
        var route = new TrackingRouteSnapshotResponse(
                "",
                List.of(request.origin(), request.destination()),
                distance,
                duration,
                Instant.now().plusSeconds(duration),
                List.of(new RouteStepResponse("Continue toward destination", "continue", distance, duration))
        );
        return new TrackingRouteResponse(route);
    }

    private Map<String, Object> waypoint(RouteLatLngRequest point) {
        return Map.of(
                "location", Map.of(
                        "latLng", Map.of(
                                "latitude", point.lat(),
                                "longitude", point.lng()
                        )
                )
        );
    }

    private int parseDuration(String duration) {
        if (duration == null || duration.isBlank()) return 0;
        var normalized = duration.endsWith("s") ? duration.substring(0, duration.length() - 1) : duration;
        var dot = normalized.indexOf('.');
        if (dot >= 0) normalized = normalized.substring(0, dot);
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private int distanceMeters(RouteLatLngRequest a, RouteLatLngRequest b) {
        var earthRadius = 6371000.0;
        var lat1 = Math.toRadians(a.lat().doubleValue());
        var lat2 = Math.toRadians(b.lat().doubleValue());
        var dLat = Math.toRadians(b.lat().doubleValue() - a.lat().doubleValue());
        var dLng = Math.toRadians(b.lng().doubleValue() - a.lng().doubleValue());
        var h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return (int) Math.round(earthRadius * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h)));
    }
}
