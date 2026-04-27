package bt.northuen.api.repository;

import bt.northuen.api.entity.Payment;
import bt.northuen.api.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = bt.northuen.api.entity.PaymentStatus.PAID")
    BigDecimal totalCollected();

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = bt.northuen.api.entity.PaymentStatus.PENDING")
    BigDecimal totalPending();

    long countByStatus(PaymentStatus status);
}
