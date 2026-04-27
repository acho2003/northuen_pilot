package bt.northuen.api.repository;

import bt.northuen.api.entity.Driver;
import bt.northuen.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByUser(User user);
    List<Driver> findByAvailableTrueOrderByUpdatedAtDesc();
}
