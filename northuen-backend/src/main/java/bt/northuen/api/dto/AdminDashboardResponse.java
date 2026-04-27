package bt.northuen.api.dto;

import java.math.BigDecimal;

public record AdminDashboardResponse(long totalOrders, long activeOrders, long deliveredOrders, long availableDrivers, BigDecimal codCollected, BigDecimal codPending) {
}
