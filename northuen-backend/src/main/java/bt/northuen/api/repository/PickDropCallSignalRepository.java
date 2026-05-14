package bt.northuen.api.repository;

import bt.northuen.api.entity.PickDropCallSession;
import bt.northuen.api.entity.PickDropCallSignal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PickDropCallSignalRepository extends JpaRepository<PickDropCallSignal, UUID> {
    List<PickDropCallSignal> findTop200ByCallOrderByCreatedAtAsc(PickDropCallSession call);
}
