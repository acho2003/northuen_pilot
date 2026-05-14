package bt.northuen.api.dto;

public record RouteStepResponse(
        String instruction,
        String maneuver,
        int distanceMeters,
        int durationSeconds
) {
}
