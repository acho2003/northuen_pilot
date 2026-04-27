package bt.northuen.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DriverResponse(
        UUID id,
        UUID userId,
        String fullName,
        String phone,
        String vehicleType,
        String licenseNumber,
        boolean available,
        BigDecimal currentLatitude,
        BigDecimal currentLongitude
) {
}
