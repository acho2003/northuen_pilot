package bt.northuen.api.dto;

import jakarta.validation.constraints.NotNull;

public record UserActiveRequest(@NotNull Boolean active) {
}
