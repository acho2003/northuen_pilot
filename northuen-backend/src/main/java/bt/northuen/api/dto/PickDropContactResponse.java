package bt.northuen.api.dto;

import java.util.UUID;

public record PickDropContactResponse(UUID userId, String name, String phone) {
}
