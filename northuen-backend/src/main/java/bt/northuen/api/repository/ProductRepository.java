package bt.northuen.api.repository;

import bt.northuen.api.entity.Product;
import bt.northuen.api.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByVendorAndAvailableTrue(Vendor vendor);
    List<Product> findByVendor(Vendor vendor);

    @Query("""
            select p from Product p
            where p.available = true
              and (:query is null or lower(p.name) like lower(concat('%', :query, '%'))
                   or lower(p.description) like lower(concat('%', :query, '%')))
            order by p.name asc
            """)
    List<Product> searchAvailable(@Param("query") String query);
}
