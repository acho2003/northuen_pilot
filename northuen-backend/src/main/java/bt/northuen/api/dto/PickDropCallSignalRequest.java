package bt.northuen.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PickDropCallSignalRequest(
        @NotBlank @Size(max = 30) String type,
        @NotBlank @Size(max = 20000) String payload
) {
}
