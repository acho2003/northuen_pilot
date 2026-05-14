package bt.northuen.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GoogleRoadsService {
    private static final double MAX_SNAP_DISTANCE_METERS = 80.0;

    private final ObjectMapper objectMapper;

    @Value("${app.google.server-api-key:}")
    private String googleServerApiKey;

    public RoadPoint snap(BigDecimal rawLat, BigDecimal rawLng) {
        if (!hasKey()) return new RoadPoint(rawLat, rawLng, false);
        try {
            var json = RestClient.create("https://roads.googleapis.com")
                    .get()
                    .uri(uri -> uri
                            .path("/v1/nearestRoads")
                            .queryParam("points", rawLat + "," + rawLng)
                            .queryParam("key", googleServerApiKey)
                            .build())
                    .retrieve()
                    .body(String.class);
            return fromRoadsResponse(json, rawLat, rawLng);
        } catch (Exception ignored) {
            return new RoadPoint(rawLat, rawLng, false);
        }
    }

    public RoadPoint snap(BigDecimal previousLat, BigDecimal previousLng, BigDecimal rawLat, BigDecimal rawLng) {
        if (!hasKey()) return new RoadPoint(rawLat, rawLng, false);
        try {
            var path = previousLat + "," + previousLng + "|" + rawLat + "," + rawLng;
            var json = RestClient.create("https://roads.googleapis.com")
                    .get()
                    .uri(uri -> uri
                            .path("/v1/snapToRoads")
                            .queryParam("path", path)
                            .queryParam("interpolate", false)
                            .queryParam("key", googleServerApiKey)
                            .build())
                    .retrieve()
                    .body(String.class);
            return fromRoadsResponse(json, rawLat, rawLng);
        } catch (Exception ignored) {
            return new RoadPoint(rawLat, rawLng, false);
        }
    }

    private RoadPoint fromRoadsResponse(String json, BigDecimal rawLat, BigDecimal rawLng) throws Exception {
        var root = objectMapper.readTree(json);
        var snappedPoints = root.path("snappedPoints");
        if (!snappedPoints.isArray() || snappedPoints.isEmpty()) {
            return new RoadPoint(rawLat, rawLng, false);
        }
        var point = snappedPoints.get(snappedPoints.size() - 1).path("location");
        var lat = BigDecimal.valueOf(point.path("latitude").asDouble());
        var lng = BigDecimal.valueOf(point.path("longitude").asDouble());
        if (distanceMeters(rawLat, rawLng, lat, lng) > MAX_SNAP_DISTANCE_METERS) {
            return new RoadPoint(rawLat, rawLng, false);
        }
        return new RoadPoint(lat, lng, true);
    }

    private boolean hasKey() {
        return googleServerApiKey != null && !googleServerApiKey.isBlank();
    }

    private double distanceMeters(BigDecimal fromLat, BigDecimal fromLng, BigDecimal toLat, BigDecimal toLng) {
        var earthRadius = 6371000.0;
        var lat1 = Math.toRadians(fromLat.doubleValue());
        var lat2 = Math.toRadians(toLat.doubleValue());
        var dLat = Math.toRadians(toLat.subtract(fromLat).doubleValue());
        var dLng = Math.toRadians(toLng.subtract(fromLng).doubleValue());
        var h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }

    public record RoadPoint(BigDecimal lat, BigDecimal lng, boolean snapped) {
    }
}
