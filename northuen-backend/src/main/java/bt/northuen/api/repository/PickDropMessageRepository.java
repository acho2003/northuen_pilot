package bt.northuen.api.repository;

import bt.northuen.api.entity.PickDropMessage;
import bt.northuen.api.entity.PickDropOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PickDropMessageRepository extends JpaRepository<PickDropMessage, UUID> {
    List<PickDropMessage> findTop80ByOrderOrderByCreatedAtAsc(PickDropOrder order);
}
