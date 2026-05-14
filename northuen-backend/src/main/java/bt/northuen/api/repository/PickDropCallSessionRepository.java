package bt.northuen.api.repository;

import bt.northuen.api.entity.PickDropCallSession;
import bt.northuen.api.entity.PickDropCallStatus;
import bt.northuen.api.entity.PickDropOrder;
import bt.northuen.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PickDropCallSessionRepository extends JpaRepository<PickDropCallSession, UUID> {
    Optional<PickDropCallSession> findFirstByOrderAndStatusInOrderByCreatedAtDesc(PickDropOrder order, Collection<PickDropCallStatus> statuses);
    Optional<PickDropCallSession> findFirstByReceiverAndStatusInOrderByCreatedAtDesc(User receiver, Collection<PickDropCallStatus> statuses);
}
