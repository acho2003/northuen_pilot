package bt.northuen.api.repository;

import bt.northuen.api.entity.Driver;
import bt.northuen.api.entity.DriverCashSettlement;
import bt.northuen.api.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverCashSettlementRepository extends JpaRepository<DriverCashSettlement, UUID> {
    Optional<DriverCashSettlement> findByPaymentId(UUID paymentId);
    List<DriverCashSettlement> findByDriverOrderByCreatedAtDesc(Driver driver);
    List<DriverCashSettlement> findByStatusOrderByCreatedAtDesc(SettlementStatus status);

    @Query("select coalesce(sum(s.amount), 0) from DriverCashSettlement s where s.status = bt.northuen.api.entity.SettlementStatus.PENDING")
    BigDecimal totalPending();

    @Query("select coalesce(sum(s.amount), 0) from DriverCashSettlement s where s.status = bt.northuen.api.entity.SettlementStatus.PAID")
    BigDecimal totalPaid();
}
