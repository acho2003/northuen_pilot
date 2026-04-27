package bt.northuen.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record CashReportResponse(BigDecimal totalCollected, BigDecimal pendingPayments, BigDecimal pendingDriverSettlement, BigDecimal paidDriverSettlement, long paidCount, long pendingCount, List<DriverCashRow> drivers) {
    public record DriverCashRow(String driverName, BigDecimal collected, BigDecimal pending) {
    }
}
