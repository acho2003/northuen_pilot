package bt.northuen.api.dto;

import bt.northuen.api.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryStatusRequest(@NotNull DeliveryStatus status) {
}
