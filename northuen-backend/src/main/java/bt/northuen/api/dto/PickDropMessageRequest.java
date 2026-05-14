package bt.northuen.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PickDropMessageRequest(@NotBlank @Size(max = 1000) String body) {
}
