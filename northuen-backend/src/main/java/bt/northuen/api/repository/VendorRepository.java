package bt.northuen.api.repository;

import bt.northuen.api.entity.User;
import bt.northuen.api.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    Optional<Vendor> findByOwner(User owner);

    @Query("""
            select v from Vendor v
            where (:category is null or lower(v.category) = lower(:category))
              and (:query is null or lower(v.name) like lower(concat('%', :query, '%'))
                   or lower(v.description) like lower(concat('%', :query, '%'))
                   or lower(v.address) like lower(concat('%', :query, '%')))
            order by v.open desc, v.name asc
            """)
    List<Vendor> search(@Param("query") String query, @Param("category") String category);
}
