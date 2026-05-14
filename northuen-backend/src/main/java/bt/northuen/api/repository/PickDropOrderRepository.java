package bt.northuen.api.repository;

import bt.northuen.api.entity.Driver;
import bt.northuen.api.entity.PickDropOrder;
import bt.northuen.api.entity.PickDropStatus;
import bt.northuen.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PickDropOrderRepository extends JpaRepository<PickDropOrder, UUID> {
    List<PickDropOrder> findByCustomerOrderByCreatedAtDesc(User customer);
    List<PickDropOrder> findByDriverOrderByCreatedAtDesc(Driver driver);
    List<PickDropOrder> findByDriverIsNullAndStatusInOrderByCreatedAtDesc(Collection<PickDropStatus> statuses);
    List<PickDropOrder> findByDriverAndStatusInOrderByCreatedAtDesc(Driver driver, Collection<PickDropStatus> statuses);
}
