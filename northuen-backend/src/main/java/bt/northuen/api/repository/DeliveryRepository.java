package bt.northuen.api.repository;

import bt.northuen.api.entity.Delivery;
import bt.northuen.api.entity.DeliveryStatus;
import bt.northuen.api.entity.Driver;
import bt.northuen.api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findByOrder(Order order);
    List<Delivery> findByDriverOrderByCreatedAtDesc(Driver driver);
    List<Delivery> findByDriverAndStatusInOrderByCreatedAtDesc(Driver driver, Collection<DeliveryStatus> statuses);
    List<Delivery> findByDriverIsNullOrderByCreatedAtDesc();
}
