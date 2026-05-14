package bt.northuen.api.repository;

import bt.northuen.api.entity.Driver;
import bt.northuen.api.entity.DriverLiveLocation;
import bt.northuen.api.entity.DriverLiveLocationId;
import bt.northuen.api.entity.PickDropOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverLiveLocationRepository extends JpaRepository<DriverLiveLocation, DriverLiveLocationId> {
    Optional<DriverLiveLocation> findByDriverAndOrder(Driver driver, PickDropOrder order);
    Optional<DriverLiveLocation> findFirstByOrderOrderByUpdatedAtDesc(PickDropOrder order);
}
