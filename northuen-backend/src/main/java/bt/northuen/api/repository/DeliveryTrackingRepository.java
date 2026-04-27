package bt.northuen.api.repository;

import bt.northuen.api.entity.Delivery;
import bt.northuen.api.entity.DeliveryTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, UUID> {
    List<DeliveryTracking> findTop20ByDeliveryOrderByCreatedAtDesc(Delivery delivery);
    Optional<DeliveryTracking> findFirstByDeliveryOrderByCreatedAtDesc(Delivery delivery);
}
