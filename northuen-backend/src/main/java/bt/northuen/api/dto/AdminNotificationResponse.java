package bt.northuen.api.dto;

public record AdminNotificationResponse(
        int sentCount,
        String target,
        String title
) {}
