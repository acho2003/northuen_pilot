package bt.northuen.api.dto;

import bt.northuen.api.entity.PickDropStatus;
import jakarta.validation.constraints.NotNull;

public record PickDropStatusRequest(@NotNull PickDropStatus status) {
}
