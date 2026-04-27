package bt.northuen.api.repository;

import bt.northuen.api.entity.Order;
import bt.northuen.api.entity.User;
import bt.northuen.api.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Order> findByVendorOrderByCreatedAtDesc(Vendor vendor);
    List<Order> findAllByOrderByCreatedAtDesc();
}
